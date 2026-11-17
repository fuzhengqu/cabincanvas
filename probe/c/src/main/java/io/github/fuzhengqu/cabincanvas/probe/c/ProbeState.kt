package io.github.fuzhengqu.cabincanvas.probe.c

object ProbeState {
    @Volatile var serviceCreated = false
    @Volatile var bound = false
    @Volatile var callback = false
    @Volatile var displayId = -1
    @Volatile var width = 0
    @Volatile var height = 0
    @Volatile var dpi = 0
    @Volatile var flags = "—"
    @Volatile var touchCount = 0
    @Volatile var lastTouch = "—"
    @Volatile var lastEvent = "未启动"
    @Volatile var audioBytes = 0L
    @Volatile var lastX = -1f
    @Volatile var lastY = -1f

    @Synchronized
    fun touch(action: Int, x: Float, y: Float) {
        touchCount += 1
        lastX = x
        lastY = y
        lastTouch = "action=$action x=${x.toInt()} y=${y.toInt()}"
    }
}
