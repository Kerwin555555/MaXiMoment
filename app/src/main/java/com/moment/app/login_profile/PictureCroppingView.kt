package com.moment.app.login_profile

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.ScaleGestureDetector.OnScaleGestureListener
import androidx.appcompat.widget.AppCompatImageView
import kotlin.math.max
import kotlin.math.min


class PictureCroppingView(context: Context?, attrs: AttributeSet?) :
    AppCompatImageView(context!!, attrs), OnScaleGestureListener {
    private
    val CIRCLE_CROP_PAINT_FLAGS: Int =
        Paint.DITHER_FLAG or Paint.FILTER_BITMAP_FLAG or Paint.ANTI_ALIAS_FLAG
    var isPreview: Boolean = true
    private
    val CIRCLE_CROP_SHAPE_PAINT: Paint = Paint(CIRCLE_CROP_PAINT_FLAGS)
    private val CIRCLE_CROP_BITMAP_PAINT = Paint(CIRCLE_CROP_PAINT_FLAGS).apply {
        setXfermode(PorterDuffXfermode(PorterDuff.Mode.SRC_IN))
    }
    private val mPaint: Paint
    private var mScaleMax = 20.0f
    private var mScaleMin = 2.0f
    private var mInitScale = 1.0f
    private val mMatrixValues = FloatArray(9)
    private var mScaleGestureDetector: ScaleGestureDetector? = null
    private val mScaleMatrix = Matrix()

    private var mLastX = 0f
    private var mLastY = 0f

    private var lastPointerCount = 0

    private val mClipBorder = Rect()

    init {
        scaleType = ScaleType.MATRIX
        mScaleGestureDetector = ScaleGestureDetector(context!!, this)
        mPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mPaint.color = Color.parseColor("#B2000000")
        mPaint.style = Paint.Style.FILL
        mPaint.isDither = true
    }

    override fun onScale(detector: ScaleGestureDetector): Boolean {
        val scale = scale
        var scaleFactor = detector.scaleFactor
        if (drawable == null) return true
        if ((scale < mScaleMax && scaleFactor > 1.0f)
            || (scale > mInitScale && scaleFactor < 1.0f)
        ) {
            if (scaleFactor * scale < mInitScale) {
                scaleFactor = mInitScale / scale
            }
            if (scaleFactor * scale > mScaleMax) {
                scaleFactor = mScaleMax / scale
            }
            mScaleMatrix.postScale(
                scaleFactor, scaleFactor,
                detector.focusX, detector.focusY
            )
            adjustBorder()
            imageMatrix = mScaleMatrix
        }
        return true
    }


    private val rectFByCurrentMatrix: RectF
        get() {
            val matrix = mScaleMatrix
            val rect = RectF()
            val d = drawable
            if (d != null && d is BitmapDrawable) {
                rect[0f, 0f, d.bitmap.width.toFloat()] = d.bitmap.height.toFloat()
                matrix.mapRect(rect)
            }
            return rect
        }

    override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
        return true
    }

    override fun onScaleEnd(detector: ScaleGestureDetector) {
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (isPreview) {
            return true
        }
        mScaleGestureDetector!!.onTouchEvent(event)

        var x = 0f
        var y = 0f
        val pointerCount = event.pointerCount

        for (i in 0 until pointerCount) {
            x += event.getX(i)
            y += event.getY(i)
        }
        x /= pointerCount.toFloat()
        y /= pointerCount.toFloat()

        if (pointerCount != lastPointerCount) {
            mLastX = x
            mLastY = y
        }

        lastPointerCount = pointerCount
        when (event.action) {
            MotionEvent.ACTION_MOVE -> {
                var dx = x - mLastX
                var dy = y - mLastY

                if (drawable != null) {
                    val rectF = rectFByCurrentMatrix
                    // 如果宽度小于屏幕宽度，则禁止左右移动
                    if (rectF.width() <= mClipBorder.width()) {
                        dx = 0f
                    }

                    // 如果高度小雨屏幕高度，则禁止上下移动
                    if (rectF.height() <= mClipBorder.height()) {
                        dy = 0f
                    }
                    mScaleMatrix.postTranslate(dx, dy)
                    adjustBorder()
                    imageMatrix = mScaleMatrix
                }

                mLastX = x
                mLastY = y
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> lastPointerCount = 0
        }
        return true
    }

    val scale: Float
        get() {
            mScaleMatrix.getValues(mMatrixValues)
            return mMatrixValues[Matrix.MSCALE_X]
        }

    /**
     * val STROKE_WIDTH = 1.dp
     * val RING_MARGIN = 10.dp
     */
    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        val width = width
        val height = height
        mClipBorder.left = RING_MARGIN
        mClipBorder.right = width - RING_MARGIN
        val borderHeight = mClipBorder.right - mClipBorder.left
        mClipBorder.top = (height - borderHeight) / 2
        mClipBorder.bottom = mClipBorder.top + borderHeight
    }

    override fun setImageDrawable(drawable: Drawable?) {
        if (drawable == null) {
            return
        }
        super.setImageDrawable(drawable)
        //Log.d(MOMENT_APP, "setup scalematix" + (drawable as? BitmapDrawable)?.bitmap?.width)
        // super.setImageDrawable(ContextCompat.getDrawable(context, R.mipmap.monster_icon_6))
        postInitImageMatrix()
    }

    private fun postInitImageMatrix() {
        if (width != 0) {
            initImageMatrix()
        } else {
            postDelayed({ initImageMatrix() }, 50)
        }
    }

    fun initImageMatrix() {
        val d = drawable
        if (d == null || (d !is BitmapDrawable)) {
            return
        }

        val bitmapWidth = d.bitmap.width
        val bitmapHeight = d.bitmap.height

        val clipBorderWidth = mClipBorder.width()
        val clipBorderHeight = mClipBorder.height()

        val imageViewWidth = width
        val imageViewHeight = height

        val scale = if (bitmapWidth * clipBorderHeight > clipBorderWidth * bitmapHeight) {
            clipBorderHeight / bitmapHeight.toFloat()
        } else {
            clipBorderWidth / bitmapWidth.toFloat()
        }

        val dx = (imageViewWidth - bitmapWidth * scale) * 0.5f
        val dy = (imageViewHeight - bitmapHeight * scale) * 0.5f
        mScaleMatrix.setScale(scale, scale)
        mScaleMatrix.postTranslate(
            (dx + 0.5f).toInt().toFloat(),
            (dy + 0.5f).toInt().toFloat()
        )
        //Log.d(MOMENT_APP, "setup scalematix" + mScaleMatrix)
        imageMatrix = mScaleMatrix

        mInitScale = scale
        mScaleMin = mInitScale * 2
        mScaleMax = mInitScale * 20
    }

    fun clipCircle(): Bitmap? {
        try {
            if (drawable == null || (drawable as BitmapDrawable).bitmap == null) {
                return null
            }

            val drawable = drawable
            val bitmap = (drawable as BitmapDrawable).bitmap

            val matrixValues = FloatArray(9)
            mScaleMatrix.getValues(matrixValues)
            val scale = matrixValues[Matrix.MSCALE_X]
            val transX = matrixValues[Matrix.MTRANS_X]
            val transY = matrixValues[Matrix.MTRANS_Y]

            val cropX = (-transX + mClipBorder.left) / scale
            val cropY = (-transY + mClipBorder.top) / scale
            val cropWidth = mClipBorder.width() / scale
            val cropHeight = mClipBorder.height() / scale

            val xStart =
                min(max(cropX.toInt().toDouble(), 0.0), bitmap.width.toDouble())
                    .toInt()
            val yStart =
                min(max(cropY.toInt().toDouble(), 0.0), bitmap.height.toDouble())
                    .toInt()
            val clippedWidth = min(
                (bitmap.width - xStart).toDouble(), cropWidth.toInt()
                    .toDouble()
            )
                .toInt()
            val clippedHeight = min(
                (bitmap.height - yStart).toDouble(), cropHeight.toInt()
                    .toDouble()
            )
                .toInt()
            //return Bitmap.createBitmap(bitmap, xStart, yStart, clippedWidth, clippedHeight, null, false)
            val result = Bitmap.createBitmap(
                clippedWidth,
                clippedHeight,
                Bitmap.Config.ARGB_8888
            ).apply {
                setHasAlpha(true)
            }
            val canvas = Canvas(result)
            // Draw a circle
            val r = clippedWidth.toFloat()/2
            canvas.drawCircle(
                r,
                r,
                r,
                CIRCLE_CROP_SHAPE_PAINT
            ) // Draw the bitmap in the circle
            val rect = Rect(xStart, yStart, xStart + clippedWidth, yStart + clippedHeight)
            canvas.drawBitmap(
                bitmap,
                rect,
                Rect(0,0,clippedWidth, clippedHeight),
                CIRCLE_CROP_BITMAP_PAINT
            )
            canvas.setBitmap(null)
            return result
        } catch (e: Throwable) {
            return null
        }
    }

    private fun adjustBorder() {
        val rect = rectFByCurrentMatrix
        var deltaX = 0f
        var deltaY = 0f

        if (rect.width() >= mClipBorder.width()) {
            if (rect.left > mClipBorder.left) {
                deltaX = -rect.left + mClipBorder.left
            }

            if (rect.right < mClipBorder.right) {
                deltaX = mClipBorder.right - rect.right
            }
        }

        if (rect.height() >= mClipBorder.height()) {
            if (rect.top > mClipBorder.top) {
                deltaY = -rect.top + mClipBorder.top
            }

            if (rect.bottom < mClipBorder.bottom) {
                deltaY = mClipBorder.bottom - rect.bottom
            }
        }

        mScaleMatrix.postTranslate(deltaX, deltaY)
    }

    fun clipOriginalBitmap(): Bitmap? {
        if (drawable == null || (drawable as BitmapDrawable).bitmap == null) {
            return null
        }

        return (drawable as BitmapDrawable).bitmap!! //已经 Center Inside 剪裁过了
    }

    override fun onDraw(canvas: Canvas) {
        try {
            super.onDraw(canvas)
        } catch (e: RuntimeException) {
            return
        }
    }
}
