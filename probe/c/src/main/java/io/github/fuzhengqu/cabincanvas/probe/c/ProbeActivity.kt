package io.github.fuzhengqu.cabincanvas.probe.c

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.text.method.ScrollingMovementMethod
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.FileProvider

/**
 * Phone-side control panel for M0. It only binds/starts the Baidu engine and
 * displays the probe log; it never tries to fake CarLife traffic itself.
 */
class ProbeActivity : Activity() {

    private val main = Handler(Looper.getMainLooper())
    private lateinit var status: TextView
    private lateinit var logView: TextView
    private lateinit var engineInfo: TextView
    private var engineConnection: ServiceConnection? = null
    private var lastDump = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ProbeLog.init(this)
        setContentView(buildUi())
        refresh()
        ProbeLog.i("ProbeActivity.onCreate version=${BuildConfig.VERSION_NAME}")
    }

    override fun onDestroy() {
        main.removeCallbacksAndMessages(null)
        runCatching {
            engineConnection?.let { unbindService(it) }
        }
        engineConnection = null
        super.onDestroy()
    }

    private fun buildUi(): ViewGroup {
        val root = ScrollView(this).apply {
            setBackgroundColor(Color.rgb(11, 13, 18))
            isFillViewport = true
        }
        val column = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(22), dp(26), dp(22), dp(28))
        }
        root.addView(column, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        column.addView(TextView(this).apply {
            text = "CabinCanvas · Probe C"
            setTextColor(Color.rgb(232, 234, 242))
            textSize = 27f
            typeface = Typeface.create("sans", Typeface.BOLD)
        })
        column.addView(TextView(this).apply {
            text = "M0 链路探针 · v${BuildConfig.VERSION_NAME}"
            setTextColor(Color.rgb(167, 173, 189))
            textSize = 13f
            setPadding(0, dp(4), 0, dp(14))
        })
        engineInfo = TextView(this).apply {
            setTextColor(Color.rgb(255, 176, 32))
            textSize = 13f
            setPadding(0, 0, 0, dp(12))
        }
        column.addView(engineInfo)

        status = TextView(this).apply {
            setTextColor(Color.rgb(232, 234, 242))
            textSize = 12.5f
            typeface = Typeface.MONOSPACE
            setPadding(dp(14), dp(14), dp(14), dp(14))
            background = rounded(Color.rgb(21, 24, 35), 18f)
        }
        column.addView(status, matchWrap())

        column.addView(space(14))
        column.addView(actionButton("启动引擎（startForegroundService）") { startEngine(foreground = true) })
        column.addView(actionButton("启动引擎（startService 备用）") { startEngine(foreground = false) })
        column.addView(actionButton("绑定引擎（bindService）") { bindEngine() })
        column.addView(actionButton("打开引擎权限页") { openEnginePermissions() })
        column.addView(actionButton("停止引擎") { stopEngine() })
        column.addView(actionButton("分享日志") { shareLog() })
        column.addView(actionButton("清空日志") {
            ProbeLog.clear()
            lastDump = ""
            refresh()
        })

        column.addView(space(14))
        column.addView(TextView(this).apply {
            text = "日志"
            setTextColor(Color.rgb(232, 234, 242))
            textSize = 15f
            typeface = Typeface.create("sans", Typeface.BOLD)
            setPadding(0, 0, 0, dp(8))
        })
        logView = TextView(this).apply {
            setTextColor(Color.rgb(167, 173, 189))
            textSize = 10.5f
            typeface = Typeface.MONOSPACE
            setPadding(dp(12), dp(12), dp(12), dp(12))
            background = rounded(Color.rgb(15, 18, 26), 14f)
            movementMethod = ScrollingMovementMethod()
            setTextIsSelectable(true)
            maxLines = 22
        }
        column.addView(logView, matchWrap())

        column.addView(space(16))
        column.addView(TextView(this).apply {
            text = "提示：先安装 com.baidu.carlife.oppo 8.8.4；插车机 USB 后，在车机点 CarLife。"
            setTextColor(Color.rgb(120, 126, 142))
            textSize = 12f
        })

        return root
    }

    private fun refresh() {
        val pkg = runCatching { packageManager.getPackageInfo(CastService.ENGINE_PACKAGE, 0) }.getOrNull()
        engineInfo.text = if (pkg == null) {
            "引擎：未安装 ${CastService.ENGINE_PACKAGE}"
        } else {
            "引擎：${pkg.versionName} (${pkg.longVersionCode})  ${CastService.ENGINE_PACKAGE}"
        }

        status.text = buildString {
            appendLine("service=${ProbeState.serviceCreated}  bound=${ProbeState.bound}  callback=${ProbeState.callback}")
            appendLine("display=${ProbeState.displayId}  ${ProbeState.width}x${ProbeState.height}@${ProbeState.dpi}dpi  flags=${ProbeState.flags}")
            appendLine("touch=${ProbeState.touchCount}  last=${ProbeState.lastTouch}")
            appendLine("audio=${ProbeState.audioBytes} bytes")
            appendLine("event=${ProbeState.lastEvent}")
            append("engineBound=${engineConnection != null}")
        }

        val dump = ProbeLog.dump()
        if (dump != lastDump) {
            lastDump = dump
            logView.text = dump
        }

        main.removeCallbacks(refreshRunnable)
        main.postDelayed(refreshRunnable, 1000L)
    }

    private val refreshRunnable = Runnable { refresh() }

    private fun startEngine(foreground: Boolean) {
        if (!isEngineInstalled()) {
            toast("未安装 ${CastService.ENGINE_PACKAGE}")
            return
        }
        val intent = engineIntent().apply {
            putExtra("bindPackageName", packageName)
            putExtra("bindClassName", CastService::class.java.name)
            putExtra("usb_type", 1)
            putExtra("connectType", "CONNECT_TYPE_AOA")
            putExtra("CONNECT_TYPE", 0)
        }
        if (foreground) {
            try {
                startForegroundService(intent)
                ProbeLog.i("engine startForegroundService ok")
            } catch (e: Exception) {
                ProbeLog.i("startForegroundService failed: ${e.javaClass.simpleName}: ${e.message}; fallback startService")
                runCatching { startService(intent) }
                    .onFailure { ProbeLog.i("startService failed: ${it.javaClass.simpleName}: ${it.message}") }
            }
        } else {
            try {
                startService(intent)
                ProbeLog.i("engine startService ok")
            } catch (e: Exception) {
                ProbeLog.i("startService failed: ${e.javaClass.simpleName}: ${e.message}")
            }
        }
    }

    private fun bindEngine() {
        if (!isEngineInstalled()) {
            toast("未安装 ${CastService.ENGINE_PACKAGE}")
            return
        }
        if (engineConnection != null) {
            ProbeLog.i("engine already bound")
            return
        }
        val connection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                ProbeLog.i("engine connected name=$name binder=${service?.interfaceDescriptor}")
                engineConnection = this
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                ProbeLog.i("engine disconnected name=$name")
                engineConnection = null
            }
        }
        try {
            bindService(engineIntent(), connection, Context.BIND_AUTO_CREATE)
            engineConnection = connection
            ProbeLog.i("bindService requested")
        } catch (e: Exception) {
            ProbeLog.i("bindService failed: ${e.javaClass.simpleName}: ${e.message}")
        }
    }

    private fun stopEngine() {
        runCatching { stopService(engineIntent()) }
        engineConnection?.let { runCatching { unbindService(it) } }
        engineConnection = null
        ProbeLog.i("engine stop requested")
    }

    private fun openEnginePermissions() {
        val intent = Intent().setComponent(
            ComponentName(CastService.ENGINE_PACKAGE, CastService.ENGINE_PERMISSION_ACTIVITY),
        )
        try {
            startActivity(intent)
        } catch (e: Exception) {
            ProbeLog.i("open permission activity failed: ${e.javaClass.simpleName}: ${e.message}")
            toast("引擎没有可打开的权限页")
        }
    }

    private fun shareLog() {
        val file = ProbeLog.file() ?: return
        if (!file.exists()) {
            toast("日志文件还不存在")
            return
        }
        val uri = FileProvider.getUriForFile(this, "$packageName.files", file)
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "CabinCanvas Probe C log")
            putExtra(Intent.EXTRA_TEXT, "CabinCanvas Probe C · v${BuildConfig.VERSION_NAME}")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(Intent.createChooser(send, "分享探针日志"))
    }

    private fun isEngineInstalled(): Boolean =
        runCatching { packageManager.getPackageInfo(CastService.ENGINE_PACKAGE, 0) }.isSuccess

    private fun engineIntent(): Intent =
        Intent().setComponent(ComponentName(CastService.ENGINE_PACKAGE, CastService.ENGINE_SERVICE))

    private fun actionButton(label: String, action: () -> Unit): Button =
        Button(this).apply {
            text = label
            isAllCaps = false
            textSize = 14f
            setOnClickListener { action() }
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(52),
            ).apply { topMargin = dp(6) }
        }

    private fun space(value: Int): TextView = TextView(this).apply {
        height = dp(value)
    }

    private fun matchWrap(): LinearLayout.LayoutParams =
        LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

    private fun rounded(color: Int, radiusDp: Float): GradientDrawable =
        GradientDrawable().apply {
            setColor(color)
            cornerRadius = dp(radiusDp.toInt()).toFloat()
        }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    private fun toast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
