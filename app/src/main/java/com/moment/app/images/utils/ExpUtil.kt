package com.moment.app.images.utils

import android.content.Context
import android.view.View

object ExpUtil {
    fun setTopPadding(view: View, context: Context) {
        val resourceId =
            context.resources.getIdentifier("status_bar_height", "dimen", "android")
        val barHeight: Int = context.resources.getDimensionPixelSize(resourceId)
        view.setPaddingRelative(0, barHeight, 0, 0)
        view.layoutParams.height = dp2px(50f, context) + barHeight
    }

    fun dp2px(dpValue: Float, context: Context): Int {
        val scale = context.resources.displayMetrics.density
        return (dpValue * scale + 0.5f).toInt()
    }
}