package com.moment.app.login_profile

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Paint.ANTI_ALIAS_FLAG
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.util.AttributeSet
import android.view.View
import com.moment.app.utils.dp

class LayerView: View {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
    private var mRadius = 0
    private var mPaint: Paint = Paint()

    init {
        setLayerType(LAYER_TYPE_HARDWARE,null)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        mRadius = (Math.min(width, height) - 2 * RING_MARGIN)/2
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        resetPaintAndDraw(PaintType.Background, canvas)
        resetPaintAndDraw(PaintType.CircularRing, canvas)
        resetPaintAndDraw(PaintType.Circle, canvas)
    }


    private fun resetPaintAndDraw(paintType: PaintType, canvas: Canvas) {
        mPaint.reset()
        when(paintType) {
            is PaintType.CircularRing -> {
                mPaint.ring()
                canvas.drawCircle(width/2f, height/2f, mRadius - STROKE_WIDTH/2F, mPaint)
            }
            is PaintType.Circle -> {
                mPaint.circle()
                canvas.drawCircle(width/2f, height/2f, mRadius.toFloat() - STROKE_WIDTH, mPaint)
            }
            is PaintType.Background -> {
                mPaint.background()
                canvas.drawRect(0f,0f,width.toFloat(), height.toFloat(), mPaint)
            }
            else -> {}
        }
    }

}

val STROKE_WIDTH = 1.dp
val RING_MARGIN = 10.dp

sealed class PaintType {
    object Background: PaintType()
    object CircularRing: PaintType()
    object Circle: PaintType()
}

fun Paint.ring() {
    flags = ANTI_ALIAS_FLAG
    color = Color.WHITE
    style = Paint.Style.STROKE
    strokeWidth = STROKE_WIDTH.toFloat()
}

fun Paint.background() {
    flags = ANTI_ALIAS_FLAG
    color = Color.parseColor("#B2000000")
    style = Paint.Style.FILL
    isDither = true
}

fun Paint.circle() {
    flags = ANTI_ALIAS_FLAG
    setXfermode(PorterDuffXfermode(PorterDuff.Mode.CLEAR))
    setColor(Color.TRANSPARENT)
    style = Paint.Style.FILL
    strokeWidth = 0f
}