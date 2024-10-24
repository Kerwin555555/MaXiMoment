package com.moment.app.login_profile

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Shader
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import com.blankj.utilcode.util.ImageUtils
import com.blankj.utilcode.util.SizeUtils
import com.moment.app.R

class TelescopeView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var bitmap: Bitmap? = null
    private val paint = Paint()
    private var shaderMatrix = Matrix()
    companion object {
        val RADIUS = 150
        val FACTOR = 3
    }

    var X = 0
    var Y = 0
    var circleX = RADIUS
    var circleY = RADIUS

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.actionMasked == MotionEvent.ACTION_DOWN) {
            X = event.x.toInt()
            Y = event.y.toInt()
        } else if (event.actionMasked == MotionEvent.ACTION_MOVE) {
            val x = event.x.toInt()
            val y = event.y.toInt()
            val dx = x - X
            val dy = y - Y

            circleX += dx
            circleY += dy

            Log.d("zhouzheng", "${circleX}:${circleY}")
            Log.d("zhouzheng", "${(1 - FACTOR) * (circleX).toFloat()}:${(1 - FACTOR) * (circleY).toFloat()}")
            shaderMatrix = Matrix()
            shaderMatrix.setTranslate((1 - FACTOR) * (circleX).toFloat(),(1 - FACTOR) * (circleY).toFloat().toFloat())
            paint.shader.setLocalMatrix(shaderMatrix)
            invalidate()
            X = x
            Y = y
        }
        return true
    }


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (bitmap == null) {
            val bmp = BitmapFactory.decodeResource(resources, R.mipmap.item6)
            bitmap = Bitmap.createScaledBitmap(bmp, width, height, false)
            val shader = BitmapShader(Bitmap.createScaledBitmap(bitmap!!,
                width * FACTOR,height * FACTOR, false), Shader.TileMode.CLAMP,
                Shader.TileMode.CLAMP)
            shaderMatrix = Matrix()
            shaderMatrix.setTranslate((1 - FACTOR) * (circleX).toFloat(),(1 - FACTOR) * (circleY).toFloat())
            shader.setLocalMatrix(shaderMatrix)
            paint.shader = shader
        }
        canvas.drawBitmap(bitmap!!, 0f, 0f, null)
        canvas.drawCircle(circleX.toFloat(), circleY.toFloat(), RADIUS.toFloat(), paint)
    }
}