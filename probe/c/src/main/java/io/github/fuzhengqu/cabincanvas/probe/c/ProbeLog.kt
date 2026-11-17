package io.github.fuzhengqu.cabincanvas.probe.c

import android.content.Context
import android.util.Log
import java.io.File
import java.text.SimpleDateFormat
import java.util.ArrayDeque
import java.util.Date
import java.util.Locale

object ProbeLog {
    private const val TAG = "CabinCanvasProbe"
    private const val MAX_LINES = 600
    private val buffer = ArrayDeque<String>(MAX_LINES)
    private val time = SimpleDateFormat("HH:mm:ss.SSS", Locale.US)
    private var file: File? = null

    @Synchronized
    fun init(context: Context) {
        if (file == null) file = File(context.filesDir, "probe.log")
    }

    @Synchronized
    fun i(message: String) {
        val line = "${time.format(Date())} $message"
        Log.i(TAG, message)
        buffer.addLast(line)
        while (buffer.size > MAX_LINES) buffer.removeFirst()
        runCatching { file?.appendText(line + "\n") }
    }

    @Synchronized
    fun dump(): String = buffer.joinToString("\n")

    @Synchronized
    fun file(): File? = file

    @Synchronized
    fun clear() {
        buffer.clear()
        runCatching { file?.writeText("") }
    }
}
