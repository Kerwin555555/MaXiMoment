package com.moment.app.utils

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.WindowInsets
import android.widget.LinearLayout

class OverrideLinearLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {

    override fun fitSystemWindows(insets: Rect?): Boolean {
        insets?.apply {
            left = 0
            top = 0
            right = 0
        }
        return super.fitSystemWindows(insets)
    }

    override fun onApplyWindowInsets(insets: WindowInsets?): WindowInsets {
        return super.onApplyWindowInsets(insets?.replaceSystemWindowInsets(0,0,0, insets.systemWindowInsetBottom))
    }
}