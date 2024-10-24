package com.moment.app.utils

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.moment.app.databinding.MomentLoadingViewBinding

class MomentLoadingView: LinearLayout {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    //protected var mValueAnimator: ValueAnimator = ValueAnimator.ofInt(0, 2)
    private val binding = MomentLoadingViewBinding.inflate(LayoutInflater.from(context), this)
   // private val argbEvaluator = ArgbEvaluator()
    init {
        orientation = VERTICAL
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
//        mValueAnimator.setDuration(200)
//        mValueAnimator.interpolator = null
//        mValueAnimator.repeatCount = ValueAnimator.INFINITE
//        mValueAnimator.repeatMode = ValueAnimator.RESTART
//        mValueAnimator.addUpdateListener { it ->
//            val v = it.animatedValue as Int
//            when (v) {
//                0 -> {
//                    binding.text.text = "Loading."
//                }
//                1 -> {
//                    binding.text.text = "Loading.."
//                }
//                else -> {
//                    binding.text.text = "Loading..."
//                }
//            }
//
//        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
//        mValueAnimator.cancel()
//        mValueAnimator.removeAllUpdateListeners()
    }


    fun start() {
        //mValueAnimator.start()
    }

    fun stop() {
       // mValueAnimator.cancel()
    }

}