package io.github.fuzhengqu.cabincanvas.data.real

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.CallLog
import android.provider.ContactsContract
import android.telecom.TelecomManager
import android.telephony.PhoneStateListener
import android.telephony.TelephonyCallback
import android.telephony.TelephonyManager
import android.text.format.DateUtils
import androidx.compose.ui.graphics.Color
import androidx.core.content.ContextCompat
import io.github.fuzhengqu.cabincanvas.data.CarCallEntry
import io.github.fuzhengqu.cabincanvas.data.CarContact
import io.github.fuzhengqu.cabincanvas.data.FakeCarData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class CarCallStatus { IDLE, RINGING, OFFHOOK }

data class CarCallState(
    val status: CarCallStatus = CarCallStatus.IDLE,
    val contact: CarContact? = null,
    val number: String? = null,
)

object DataPermissions {
    fun hasNotificationAccess(context: Context): Boolean =
        androidx.core.app.NotificationManagerCompat.getEnabledListenerPackages(context)
            .contains(context.packageName)

    fun hasPhoneAccess(context: Context): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) ==
            PackageManager.PERMISSION_GRANTED

    fun hasContactsAccess(context: Context): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) ==
            PackageManager.PERMISSION_GRANTED

    fun hasCallLogAccess(context: Context): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALL_LOG) ==
            PackageManager.PERMISSION_GRANTED
}

/**
 * Contacts, call log and live call state. Everything degrades to the bundled
 * demo contacts when permissions are missing, so the car UI never goes blank.
 */
class PhoneRepository(private val context: Context) {
    private val _contacts = MutableStateFlow(FakeCarData.contacts)
    val contacts: StateFlow<List<CarContact>> = _contacts.asStateFlow()

    private val _recent = MutableStateFlow<List<io.github.fuzhengqu.cabincanvas.data.CarCallEntry>>(emptyList())
    val recent: StateFlow<List<io.github.fuzhengqu.cabincanvas.data.CarCallEntry>> = _recent.asStateFlow()

    private val _call = MutableStateFlow(CarCallState())
    val call: StateFlow<CarCallState> = _call.asStateFlow()

    private val telephony = context.getSystemService(TelephonyManager::class.java)
    private val telecom = context.getSystemService(TelecomManager::class.java)
    private var telephonyCallback: TelephonyCallback? = null
    private var legacyListener: PhoneStateListener? = null
    private var registered = false

    fun start() {
        loadContacts()
        loadRecent()
        registerTelephony()
    }

    fun refresh() {
        loadContacts()
        loadRecent()
        registerTelephony()
    }

    fun stop() {
        unregisterTelephony()
    }

    @SuppressLint("MissingPermission")
    fun answer() {
        runCatching { telecom?.acceptRingingCall() }
        _call.value = _call.value.copy(status = CarCallStatus.OFFHOOK)
    }

    @SuppressLint("MissingPermission")
    fun hangup() {
        runCatching { telecom?.endCall() }
        _call.value = CarCallState()
    }

    fun demoIncoming(contact: CarContact) {
        _call.value = CarCallState(CarCallStatus.RINGING, contact, contact.number)
    }

    fun demoAnswer() {
        val current = _call.value
        _call.value = current.copy(status = CarCallStatus.OFFHOOK)
    }

    fun dial(number: String) {
        val cleaned = number.filter { it.isDigit() || it == '+' || it == '*' || it == '#' }
        if (cleaned.isBlank()) return
        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$cleaned")).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        runCatching { context.startActivity(intent) }
    }

    @SuppressLint("MissingPermission")
    private fun loadContacts() {
        if (!DataPermissions.hasContactsAccess(context)) {
            if (_contacts.value.isEmpty()) _contacts.value = FakeCarData.contacts
            return
        }
        val result = runCatching {
            val projection = arrayOf(
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER,
            )
            context.contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                projection,
                null,
                null,
                "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} ASC",
            )?.use { cursor ->
                val nameIndex = cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                val numberIndex = cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER)
                val seen = HashSet<String>()
                val items = ArrayList<CarContact>()
                while (cursor.moveToNext() && items.size < 24) {
                    val name = cursor.getString(nameIndex)?.trim().orEmpty()
                    val number = cursor.getString(numberIndex)?.trim().orEmpty()
                    if (name.isBlank() || number.isBlank()) continue
                    if (!seen.add(number)) continue
                    items += CarContact(
                        name = name,
                        number = number,
                        letter = name.take(1),
                        color = contactColor(number),
                    )
                }
                items
            }.orEmpty()
        }.getOrDefault(emptyList())
        if (result.isNotEmpty()) _contacts.value = result
    }

    @SuppressLint("MissingPermission")
    private fun loadRecent() {
        if (!DataPermissions.hasCallLogAccess(context)) return
        val items = runCatching {
            val projection = arrayOf(
                CallLog.Calls.CACHED_NAME,
                CallLog.Calls.NUMBER,
                CallLog.Calls.TYPE,
                CallLog.Calls.DATE,
            )
            context.contentResolver.query(
                CallLog.Calls.CONTENT_URI,
                projection,
                null,
                null,
                "${CallLog.Calls.DATE} DESC",
            )?.use { cursor ->
                val nameIndex = cursor.getColumnIndexOrThrow(CallLog.Calls.CACHED_NAME)
                val numberIndex = cursor.getColumnIndexOrThrow(CallLog.Calls.NUMBER)
                val typeIndex = cursor.getColumnIndexOrThrow(CallLog.Calls.TYPE)
                val dateIndex = cursor.getColumnIndexOrThrow(CallLog.Calls.DATE)
                val result = ArrayList<CarCallEntry>()
                while (cursor.moveToNext() && result.size < 20) {
                    val number = cursor.getString(numberIndex)?.trim().orEmpty()
                    if (number.isBlank()) continue
                    val name = cursor.getString(nameIndex)?.trim().takeUnless { it.isNullOrBlank() } ?: number
                    val date = cursor.getLong(dateIndex)
                    val type = cursor.getInt(typeIndex)
                    val contact = _contacts.value.firstOrNull {
                        it.number.filterDigits().takeLast(7) == number.filterDigits().takeLast(7)
                    } ?: CarContact(name, number, name.take(1), contactColor(number))
                    val typeLabel = when (type) {
                        CallLog.Calls.INCOMING_TYPE -> "来电"
                        CallLog.Calls.OUTGOING_TYPE -> "去电"
                        CallLog.Calls.MISSED_TYPE -> "未接"
                        else -> "通话"
                    }
                    val time = DateUtils.getRelativeTimeSpanString(
                        date,
                        System.currentTimeMillis(),
                        DateUtils.MINUTE_IN_MILLIS,
                    ).toString()
                    result += CarCallEntry(contact, "$typeLabel · $time", date)
                }
                result
            }.orEmpty()
        }.getOrDefault(emptyList())
        if (items.isNotEmpty()) _recent.value = items
    }

    @SuppressLint("MissingPermission")
    private fun registerTelephony() {
        if (registered || !DataPermissions.hasPhoneAccess(context) || telephony == null) return
        runCatching {
            if (Build.VERSION.SDK_INT >= 31) {
                val callback = object : TelephonyCallback(), TelephonyCallback.CallStateListener {
                    override fun onCallStateChanged(state: Int) = handleCallState(state, null)
                }
                telephonyCallback = callback
                telephony.registerTelephonyCallback(context.mainExecutor, callback)
            } else {
                @Suppress("DEPRECATION")
                val listener = object : PhoneStateListener() {
                    @Deprecated("Deprecated in Java")
                    override fun onCallStateChanged(state: Int, phoneNumber: String?) {
                        handleCallState(state, phoneNumber)
                    }
                }
                legacyListener = listener
                @Suppress("DEPRECATION")
                telephony.listen(listener, PhoneStateListener.LISTEN_CALL_STATE)
            }
            registered = true
        }
    }

    private fun unregisterTelephony() {
        if (!registered) return
        runCatching {
            if (Build.VERSION.SDK_INT >= 31) {
                telephonyCallback?.let { telephony?.unregisterTelephonyCallback(it) }
            } else {
                @Suppress("DEPRECATION")
                legacyListener?.let { telephony?.listen(it, PhoneStateListener.LISTEN_NONE) }
            }
        }
        telephonyCallback = null
        legacyListener = null
        registered = false
    }

    private fun handleCallState(state: Int, incomingNumber: String?) {
        when (state) {
            TelephonyManager.CALL_STATE_RINGING -> {
                val number = incomingNumber
                val contact = _contacts.value.firstOrNull { it.number.filterDigits().endsWith(number.orEmpty().filterDigits().takeLast(7)) }
                    ?: number?.let { CarContact("未知来电", it, "?", contactColor(it)) }
                _call.value = CarCallState(CarCallStatus.RINGING, contact, number)
            }
            TelephonyManager.CALL_STATE_OFFHOOK -> {
                val current = _call.value
                _call.value = current.copy(status = CarCallStatus.OFFHOOK)
            }
            TelephonyManager.CALL_STATE_IDLE -> {
                _call.value = CarCallState()
            }
        }
    }

    private fun contactColor(value: String): Color {
        val colors = listOf(
            Color(0xFF2F6BFF), Color(0xFF19A974), Color(0xFFE32E27),
            Color(0xFFFFB020), Color(0xFF8B5CF6), Color(0xFF0EA5E9),
        )
        return colors[value.hashCode().mod(colors.size)]
    }

    private fun String.filterDigits(): String = filter { it.isDigit() }
}
