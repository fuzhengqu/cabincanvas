package io.github.fuzhengqu.cabincanvas.probe.c

import android.app.Presentation
import android.app.Service
import android.content.Intent
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.os.Binder
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.Parcel
import android.view.MotionEvent
import android.view.Surface

/**
 * Minimal ICarLifeCastManager side of the Baidu CarLife "mixing" bridge.
 *
 * The engine is expected to bind this service, call setCastCallback(), then
 * call prepareCast(CastConfig{width,height,dpi,Surface}) after the head unit
 * handshake. All transaction codes are provisional: the probe logs every
 * descriptor/code so M0 can confirm them against the real APK.
 */
class CastService : Service() {

    companion object {
        const val ENGINE_PACKAGE = "com.baidu.carlife.oppo"
        const val ENGINE_SERVICE = "com.baidu.carlife.service.CarlifeConnectService"
        const val ENGINE_PERMISSION_ACTIVITY = "com.baidu.carlife.mix.oppo.OppoPermissionActivity"
        const val MANAGER_DESCRIPTOR = "com.baidu.carlife.mixing.aidl.ICarLifeCastManager"
        const val CALLBACK_DESCRIPTOR = "com.baidu.carlife.mixing.aidl.ICarLifeCastCallback"

        // @hide constants, stable values from DisplayManager.
        private const val FLAG_SUPPORTS_TOUCH = 1 shl 6 // VIRTUAL_DISPLAY_FLAG_SUPPORTS_TOUCH
        private const val FLAG_DESTROY_CONTENT = 1 shl 8 // VIRTUAL_DISPLAY_FLAG_DESTROY_CONTENT
    }

    private val main = Handler(Looper.getMainLooper())
    private val manager = CastManagerBinder()
    private var virtualDisplay: VirtualDisplay? = null
    private var presentation: Presentation? = null
    private var callback: IBinder? = null
    private var audioPackets = 0

    override fun onCreate() {
        super.onCreate()
        ProbeLog.init(this)
        ProbeState.serviceCreated = true
        ProbeState.lastEvent = "CastService 已创建"
        ProbeLog.i("CastService.onCreate")
    }

    override fun onBind(intent: Intent?): IBinder {
        ProbeState.bound = true
        ProbeState.lastEvent = "引擎已绑定 CastService"
        ProbeLog.i("CastService.onBind action=${intent?.action} component=${intent?.component}")
        return manager
    }

    override fun onUnbind(intent: Intent?): Boolean {
        ProbeState.bound = false
        ProbeLog.i("CastService.onUnbind")
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        ProbeLog.i("CastService.onDestroy")
        releaseCast()
        ProbeState.serviceCreated = false
        ProbeState.bound = false
        ProbeState.callback = false
        callback = null
        super.onDestroy()
    }

    fun prepareCast(width: Int, height: Int, dpi: Int, surface: Surface?) {
        ProbeLog.i("manager.prepareCast w=$width h=$height dpi=$dpi surface=${surface?.isValid}")
        main.post { createCast(width, height, dpi, surface) }
    }

    fun stopCast() {
        ProbeLog.i("manager.stopCast")
        main.post {
            releaseCast()
            ProbeState.lastEvent = "stopCast"
        }
    }

    fun setCastCallback(binder: IBinder?) {
        callback = binder
        ProbeState.callback = binder != null
        ProbeState.lastEvent = "拿到 ICarLifeCastCallback"
        ProbeLog.i("manager.setCastCallback descriptor=${binder?.interfaceDescriptor}")
        binder?.let {
            runCatching {
                it.linkToDeath({
                    ProbeLog.i("engine callback died")
                    callback = null
                    ProbeState.callback = false
                }, 0)
            }
        }
        main.postDelayed({
            callbackOnServiceReady()
            main.postDelayed({
                val touch = TouchListenerBinder()
                callbackTransact(3) { it.writeStrongBinder(touch) }
                val audio = AudioListenerBinder()
                callbackTransact(12) { it.writeStrongBinder(audio) }
            }, 400)
        }, 150)
    }

    fun setSystemAudioCallback(binder: IBinder?) {
        ProbeLog.i("manager.setSystemAudioCallback descriptor=${binder?.interfaceDescriptor}")
    }

    fun invokeMethod(intent: Intent): Bundle {
        ProbeLog.i("manager.invokeMethod action=${intent.action} extras=${intent.extras?.keySet()}")
        return Bundle().apply {
            putBoolean("ok", true)
            putString("message", "CabinCanvas probe")
        }
    }

    private fun createCast(width: Int, height: Int, dpi: Int, surface: Surface?) {
        releaseCast()
        ProbeState.width = width
        ProbeState.height = height
        ProbeState.dpi = dpi
        ProbeState.lastEvent = "prepareCast 收到"

        if (width <= 0 || height <= 0 || dpi <= 0) {
            ProbeState.lastEvent = "CastConfig 尺寸无效"
            ProbeLog.i("prepareCast invalid config w=$width h=$height dpi=$dpi")
            return
        }
        if (surface == null || !surface.isValid) {
            ProbeState.lastEvent = "CastConfig Surface 无效"
            ProbeLog.i("prepareCast surface invalid")
            return
        }

        val displayManager = getSystemService(DisplayManager::class.java)
        val attempts = listOf(
            "full" to fullFlags(),
            "public" to (
                DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC or
                    DisplayManager.VIRTUAL_DISPLAY_FLAG_PRESENTATION or
                    DisplayManager.VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY or
                    FLAG_SUPPORTS_TOUCH or
                    FLAG_DESTROY_CONTENT
                ),
            "private" to (
                DisplayManager.VIRTUAL_DISPLAY_FLAG_PRESENTATION or
                    DisplayManager.VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY or
                    FLAG_SUPPORTS_TOUCH or
                    FLAG_DESTROY_CONTENT
                ),
        )

        var display: VirtualDisplay? = null
        var used = "none"
        for ((name, flags) in attempts) {
            try {
                display = displayManager.createVirtualDisplay(
                    "CabinCanvasProbe",
                    width,
                    height,
                    dpi,
                    surface,
                    flags,
                )
                used = name
                ProbeLog.i("createVirtualDisplay ok flags=$name 0x${flags.toString(16)}")
                break
            } catch (e: Exception) {
                ProbeLog.i("createVirtualDisplay $name failed: ${e.javaClass.simpleName}: ${e.message}")
            }
        }

        if (display == null) {
            ProbeState.lastEvent = "VirtualDisplay 创建失败"
            return
        }

        virtualDisplay = display
        ProbeState.flags = used
        ProbeState.displayId = display.display.displayId
        ProbeState.lastEvent = "VirtualDisplay 已创建 ($used)"

        try {
            val p = Presentation(this, display.display, R.style.Theme_CabinCanvas_Probe)
            p.setContentView(ProbeView(this))
            p.show()
            presentation = p
            ProbeLog.i("Presentation shown on display=${display.display.displayId}")
        } catch (e: Exception) {
            ProbeLog.i("Presentation failed: ${e.javaClass.simpleName}: ${e.message}")
        }

        callbackOnCastPrepared(display.display.displayId)
    }

    private fun fullFlags(): Int {
        var flags = DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC or
            DisplayManager.VIRTUAL_DISPLAY_FLAG_PRESENTATION or
            DisplayManager.VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY or
            FLAG_SUPPORTS_TOUCH or
            FLAG_DESTROY_CONTENT
        if (Build.VERSION.SDK_INT >= 33) flags = flags or 1024 // TRUSTED
        if (Build.VERSION.SDK_INT >= 34) flags = flags or (1 shl 14) // OWN_FOCUS
        return flags
    }

    private fun releaseCast() {
        runCatching { presentation?.dismiss() }
        presentation = null
        runCatching { virtualDisplay?.release() }
        virtualDisplay = null
        ProbeState.displayId = -1
        ProbeState.flags = "—"
    }

    private fun callbackOnServiceReady() {
        callbackTransact(1)
    }

    private fun callbackOnCastPrepared(displayId: Int) {
        callbackTransact(2) { it.writeInt(displayId) }
    }

    private fun callbackTransact(code: Int, writer: (Parcel) -> Unit = {}) {
        val cb = callback ?: run {
            ProbeLog.i("callback code=$code skipped: no callback")
            return
        }
        val data = Parcel.obtain()
        val reply = Parcel.obtain()
        try {
            val descriptor = cb.interfaceDescriptor ?: CALLBACK_DESCRIPTOR
            data.writeInterfaceToken(descriptor)
            writer(data)
            val ok = cb.transact(code, data, reply, 0)
            if (reply.dataSize() > 0) runCatching { reply.readException() }
            ProbeLog.i("callback code=$code ok=$ok")
        } catch (e: Exception) {
            ProbeLog.i("callback code=$code failed: ${e.javaClass.simpleName}: ${e.message}")
        } finally {
            data.recycle()
            reply.recycle()
        }
    }

    inner class CastManagerBinder : Binder() {
        override fun onTransact(code: Int, data: Parcel, reply: Parcel?, flags: Int): Boolean {
            val start = data.dataPosition()
            val token = runCatching { data.readString() }.getOrNull()
            data.setDataPosition(start)
            ProbeLog.i("manager.onTransact code=$code token=$token size=${data.dataSize()}")
            return try {
                when (code) {
                    1 -> {
                        data.readString()
                        val width = data.readInt()
                        val height = data.readInt()
                        val dpi = data.readInt()
                        val surface = runCatching { Surface.CREATOR.createFromParcel(data) }.getOrNull()
                        prepareCast(width, height, dpi, surface)
                        reply?.writeNoException()
                        true
                    }
                    2 -> {
                        data.readString()
                        stopCast()
                        reply?.writeNoException()
                        true
                    }
                    3 -> {
                        data.readString()
                        setSystemAudioCallback(data.readStrongBinder())
                        reply?.writeNoException()
                        true
                    }
                    4 -> {
                        data.readString()
                        setCastCallback(data.readStrongBinder())
                        reply?.writeNoException()
                        true
                    }
                    5 -> {
                        data.readString()
                        val intent = Intent.CREATOR.createFromParcel(data)
                        val result = invokeMethod(intent)
                        reply?.writeNoException()
                        reply?.let { result.writeToParcel(it, 0) }
                        true
                    }
                    else -> {
                        ProbeLog.i("manager.onTransact unknown code=$code")
                        reply?.writeNoException()
                        true
                    }
                }
            } catch (e: Exception) {
                ProbeLog.i("manager.onTransact code=$code failed: ${e.javaClass.simpleName}: ${e.message}")
                runCatching { reply?.let { it.writeException(e) } }
                true
            }
        }
    }

    inner class TouchListenerBinder : Binder() {
        override fun onTransact(code: Int, data: Parcel, reply: Parcel?, flags: Int): Boolean {
            if (code == 1) {
                runCatching { data.readString() }
                val event = MotionEvent.CREATOR.createFromParcel(data)
                ProbeState.touch(event.actionMasked, event.x, event.y)
                ProbeLog.i("remote touch action=${event.actionMasked} x=${event.x.toInt()} y=${event.y.toInt()}")
                reply?.writeNoException()
                reply?.writeInt(1)
                return true
            }
            return true
        }
    }

    inner class AudioListenerBinder : Binder() {
        override fun onTransact(code: Int, data: Parcel, reply: Parcel?, flags: Int): Boolean {
            if (code == 1) {
                runCatching { data.readString() }
                val bytes = runCatching { data.createByteArray() }.getOrNull()
                val a = runCatching { data.readInt() }.getOrDefault(0)
                val b = runCatching { data.readInt() }.getOrDefault(0)
                val c = runCatching { data.readInt() }.getOrDefault(0)
                val size = bytes?.size ?: 0
                ProbeState.audioBytes += size
                audioPackets += 1
                if (audioPackets == 1 || audioPackets % 100 == 0) {
                    ProbeLog.i("remote audio packet=$audioPackets bytes=$size a=$a b=$b c=$c total=${ProbeState.audioBytes}")
                }
                reply?.writeNoException()
                return true
            }
            return true
        }
    }
}
