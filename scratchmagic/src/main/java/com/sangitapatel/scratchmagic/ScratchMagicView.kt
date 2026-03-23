/**
 * ScratchMagicView.kt
 *
 * Author  : Sangita Patel
 * GitHub  : https://github.com/sangitapatel
 * License : MIT (see LICENSE file)
 *
 * Original work — written from scratch by Sangita Patel.
 * No portion of this file is copied or derived from any other library.
 */

package com.sangitapatel.scratchmagic

import android.animation.TimeInterpolator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RectF
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat

/**
 * A View that shows a scratchable "foil" layer on top of any content.
 *
 * Foil fill modes (applied in this priority order)
 * ─────────────────────────────────────────────────
 *   1. [foilDrawable] set via XML or [setFoilDrawable]
 *   2. [foilColor] solid colour (default = silver #BDBDBD)
 */
class ScratchMagicView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : View(context, attrs, defStyle) {


    interface ScratchListener {
        fun onProgress(view: ScratchMagicView, percent: Float)
        fun onDone(view: ScratchMagicView)
    }


    var brushRadius: Float = DEFAULT_RADIUS
        set(value) {
            field = value.coerceAtLeast(1f)
            touchPaint.strokeWidth = field * 2f
        }

    var threshold: Float = DEFAULT_THRESHOLD
        set(value) { field = value.coerceIn(0f, 100f) }

    @ColorInt
    var foilColor: Int = DEFAULT_FOIL_COLOR
        set(value) {
            field = value
            if (width > 0 && height > 0 && foilDrawable == null)
                buildFoilLayer()
        }

    var foilDrawable: Drawable? = null
        set(value) {
            field = value
            if (width > 0 && height > 0) buildFoilLayer()
        }

    var animateReveal: Boolean = true
    var animateDuration: Long  = DEFAULT_ANIM_MS
    var sampleStep: Int        = 4
    var listener: ScratchListener? = null


    private var foilBitmap: Bitmap? = null
    private var foilCanvas: Canvas? = null

    private val strokePath = Path()
    private var prevX = 0f
    private var prevY = 0f

    private val touchPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        xfermode    = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        style       = Paint.Style.STROKE
        strokeCap   = Paint.Cap.ROUND
        strokeJoin  = Paint.Join.ROUND
        strokeWidth = DEFAULT_RADIUS * 2f
    }

    private val blitPaint = Paint(Paint.FILTER_BITMAP_FLAG)
    private val foilRect  = RectF()

    private var isDone      = false
    private var revealAnim: ValueAnimator? = null
    private var lastPercent = -1f


    init {
        setLayerType(LAYER_TYPE_HARDWARE, null)
        readXmlAttributes(attrs)
    }

    private fun readXmlAttributes(attrs: AttributeSet?) {
        if (attrs == null) return
        val ta = context.obtainStyledAttributes(attrs, R.styleable.ScratchMagicView)
        try {
            brushRadius     = ta.getDimension (R.styleable.ScratchMagicView_smv_brushRadius,   DEFAULT_RADIUS)
            threshold       = ta.getFloat     (R.styleable.ScratchMagicView_smv_threshold,      DEFAULT_THRESHOLD)
            foilColor       = ta.getColor     (R.styleable.ScratchMagicView_smv_foilColor,       DEFAULT_FOIL_COLOR)
            animateReveal   = ta.getBoolean   (R.styleable.ScratchMagicView_smv_animateReveal,   true)
            animateDuration = ta.getInt       (R.styleable.ScratchMagicView_smv_animateDuration, DEFAULT_ANIM_MS.toInt()).toLong()
            val resId       = ta.getResourceId(R.styleable.ScratchMagicView_smv_foilDrawable,    0)
            if (resId != 0) foilDrawable = ContextCompat.getDrawable(context, resId)
        } finally {
            ta.recycle()
        }
        touchPaint.strokeWidth = brushRadius * 2f
    }


    override fun onSizeChanged(w: Int, h: Int, oldW: Int, oldH: Int) {
        super.onSizeChanged(w, h, oldW, oldH)
        if (w > 0 && h > 0) buildFoilLayer()
    }

    private fun buildFoilLayer() {
        val w = width
        val h = height
        if (w <= 0 || h <= 0) return

        foilBitmap?.recycle()

        val fresh = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val c     = Canvas(fresh)

        if (foilDrawable != null) {
            drawDrawableOnto(c, foilDrawable!!, w, h)
        } else {
            c.drawColor(foilColor)
        }

        foilBitmap = fresh
        foilCanvas = c
        isDone      = false
        lastPercent = -1f
        strokePath.reset()
        invalidate()
    }

    private fun drawDrawableOnto(c: Canvas, d: Drawable, w: Int, h: Int) {
        when (d) {
            is BitmapDrawable -> {
                val bmp    = d.bitmap ?: return
                val scaled = Bitmap.createScaledBitmap(bmp, w, h, true)
                c.drawBitmap(scaled, 0f, 0f, null)
                if (scaled !== bmp) scaled.recycle()
            }
            else -> {
                d.setBounds(0, 0, w, h)
                d.draw(c)
            }
        }
    }


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val bmp = foilBitmap ?: return
        foilRect.set(0f, 0f, width.toFloat(), height.toFloat())
        canvas.drawBitmap(bmp, null, foilRect, blitPaint)
    }


    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (isDone) return false
        val x = event.x
        val y = event.y
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                strokePath.reset()
                strokePath.moveTo(x, y)
                prevX = x; prevY = y
                eraseAt(x, y)
            }
            MotionEvent.ACTION_MOVE -> {
                val midX = (prevX + x) / 2f
                val midY = (prevY + y) / 2f
                strokePath.quadTo(prevX, prevY, midX, midY)
                foilCanvas?.drawPath(strokePath, touchPaint)
                prevX = x; prevY = y
                invalidate()
                reportProgress()
            }
            MotionEvent.ACTION_UP,
            MotionEvent.ACTION_CANCEL -> {
                strokePath.reset()
                reportProgress()
            }
        }
        parent?.requestDisallowInterceptTouchEvent(true)
        return true
    }

    private fun eraseAt(x: Float, y: Float) {
        val p = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
            style    = Paint.Style.FILL
        }
        foilCanvas?.drawCircle(x, y, brushRadius, p)
        invalidate()
    }


    private fun reportProgress() {
        val pct = computeRevealPercent()
        if (pct == lastPercent) return
        lastPercent = pct
        listener?.onProgress(this, pct)
        if (!isDone && pct >= threshold) markDone()
    }

    private fun computeRevealPercent(): Float {
        val bmp = foilBitmap ?: return 0f
        var cleared = 0; var total = 0
        var row = 0
        while (row < bmp.height) {
            var col = 0
            while (col < bmp.width) {
                if (Color.alpha(bmp.getPixel(col, row)) == 0) cleared++
                total++
                col += sampleStep
            }
            row += sampleStep
        }
        return if (total == 0) 0f else cleared * 100f / total
    }


    private fun markDone() {
        isDone = true
        listener?.onDone(this)
        if (animateReveal) startRevealAnimation()
    }

    private fun startRevealAnimation() {
        revealAnim?.cancel()
        val cx = width / 2f; val cy = height / 2f
        val maxR   = Math.hypot(width.toDouble(), height.toDouble()).toFloat()
        val startR = maxR * (lastPercent / 100f) * 0.5f
        val p = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
            style    = Paint.Style.FILL
        }
        revealAnim = ValueAnimator.ofFloat(startR, maxR).apply {
            duration     = animateDuration
            interpolator = EaseOutQuart()
            addUpdateListener { anim ->
                foilCanvas?.drawCircle(cx, cy, anim.animatedValue as Float, p)
                invalidate()
            }
            start()
        }
    }


    fun reveal(animate: Boolean = true) {
        if (animate) {
            isDone = true
            startRevealAnimation()
        } else {
            revealAnim?.cancel()
            foilCanvas?.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
            isDone = true
            invalidate()
        }
    }

    fun reset() {
        revealAnim?.cancel()
        revealAnim = null
        buildFoilLayer()
    }

    fun setFoilDrawable(@DrawableRes resId: Int) {
        foilDrawable = ContextCompat.getDrawable(context, resId)
    }

    val revealPercent: Float  get() = computeRevealPercent()
    val isFullyRevealed: Boolean get() = isDone


    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        revealAnim?.cancel()
        revealAnim = null
        foilBitmap?.recycle()
        foilBitmap = null
        foilCanvas = null
    }


    companion object {
        const val DEFAULT_RADIUS    = 44f
        const val DEFAULT_THRESHOLD = 60f
        const val DEFAULT_ANIM_MS   = 450L
        val DEFAULT_FOIL_COLOR      = Color.parseColor("#BDBDBD")
    }

    private class EaseOutQuart : TimeInterpolator {
        override fun getInterpolation(t: Float): Float {
            val u = 1f - t; return 1f - u * u * u * u
        }
    }
}
