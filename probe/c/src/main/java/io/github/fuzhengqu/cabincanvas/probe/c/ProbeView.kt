package io.github.fuzhengqu.cabincanvas.probe.c

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.view.MotionEvent
import android.view.View

/**
 * Diagnostic surface for M0. It draws a colour field, five large hit targets and the
 * current transport state. It is intentionally dependency-free and survives on a
 * Presentation with an unknown DPI.
 */
class ProbeView(context: Context) : View(context) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(232, 234, 242)
        typeface = Typeface.create("sans", Typeface.BOLD)
    }
    private val statusPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(167, 173, 189)
        typeface = Typeface.MONOSPACE
    }
    private val buttonPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(47, 107, 255)
    }
    private val buttonTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create("sans", Typeface.BOLD)
    }
    private val accentPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(25, 169, 116)
    }
    private val touchPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(255, 176, 32)
    }
    private val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
    }

    private val buttons = arrayOf("A", "B", "C", "D", "E")

    init {
        setBackgroundColor(Color.rgb(11, 13, 18))
        isFocusable = true
        isClickable = true
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat()
        val h = height.toFloat()
        if (w <= 0f || h <= 0f) return
        val s = (w / 1280f).coerceAtLeast(0.5f)

        canvas.drawColor(Color.rgb(11, 13, 18))

        titlePaint.textSize = 42f * s
        canvas.drawText("CabinCanvas · Probe C", 48f * s, 66f * s, titlePaint)
        statusPaint.textSize = 21f * s
        canvas.drawText(
            "display=${ProbeState.displayId}  ${ProbeState.width}x${ProbeState.height}@${ProbeState.dpi}dpi",
            48f * s,
            104f * s,
            statusPaint,
        )
        canvas.drawText(
            "flags=${ProbeState.flags}  callback=${ProbeState.callback}  audio=${ProbeState.audioBytes}B",
            48f * s,
            132f * s,
            statusPaint,
        )
        canvas.drawText(
            "touches=${ProbeState.touchCount}  last=${ProbeState.lastTouch}",
            48f * s,
            160f * s,
            statusPaint,
        )
        canvas.drawText(
            "event=${ProbeState.lastEvent}",
            48f * s,
            188f * s,
            statusPaint,
        )

        val margin = 48f * s
        val gap = 18f * s
        val buttonW = (w - margin * 2 - gap * 4) / 5f
        val buttonH = h * 0.20f
        val top = h * 0.60f
        buttons.forEachIndexed { index, label ->
            val left = margin + index * (buttonW + gap)
            val rect = RectF(left, top, left + buttonW, top + buttonH)
            canvas.drawRoundRect(rect, 22f * s, 22f * s, buttonPaint)
            buttonTextPaint.textSize = 58f * s
            val baseline = rect.centerY() - (buttonTextPaint.descent() + buttonTextPaint.ascent()) / 2f
            canvas.drawText(label, rect.centerX(), baseline, buttonTextPaint)
        }

        canvas.drawRect(
            margin,
            h - 34f * s,
            w - margin,
            h - 28f * s,
            accentPaint,
        )

        if (ProbeState.lastX >= 0f && ProbeState.lastY >= 0f) {
            canvas.drawCircle(ProbeState.lastX, ProbeState.lastY, 26f * s, touchPaint)
            canvas.drawCircle(ProbeState.lastX, ProbeState.lastY, 8f * s, dotPaint)
        }

        postInvalidateDelayed(500L)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        ProbeState.touch(event.actionMasked, event.x, event.y)
        ProbeLog.i("view touch action=${event.actionMasked} x=${event.x.toInt()} y=${event.y.toInt()}")
        invalidate()
        return true
    }
}
