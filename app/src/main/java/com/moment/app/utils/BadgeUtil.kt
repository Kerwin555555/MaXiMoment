package com.moment.app.utils

import android.view.Gravity
import android.view.ViewGroup
import android.widget.TextView
import com.blankj.utilcode.util.SizeUtils


fun TextView.setBadgeBackground(count: Int) {
    if (count >= 10) {
        gravity = Gravity.NO_GRAVITY
        applyPaddingsWithDefaultZero(start = 5.dp, end = 5.dp, top = 2.dp, bottom = 2.dp)
        layoutParams = ViewGroup.LayoutParams(-2, -2)
    } else {
        applyPaddingsWithDefaultZero(start = 2.dp, end = 2.dp, top = 2.dp, bottom = 2.dp)
        SizeUtils.measureView(this)
        val w = measuredWidth
        val h = measuredHeight
        val max = Math.max(w, h)
        gravity = Gravity.CENTER
        this.layoutParams.width = max
        this.layoutParams.height = max
        this.layoutParams = this.layoutParams
    }
    text = "${count}"
}