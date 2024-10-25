package com.moment.app.utils

import android.content.res.ColorStateList
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import com.blankj.utilcode.util.SizeUtils
import com.gyf.immersionbar.ImmersionBar
import java.lang.Exception


// TextView 设置textColor扩展函数
internal fun TextView.applyEnabledColorStateList(@ColorRes enableId: Int,
                                                 @ColorRes disableId: Int) {
    val status: Array<IntArray> =
        arrayOf<IntArray>(intArrayOf(android.R.attr.state_enabled), intArrayOf())
    val colors: IntArray = intArrayOf(
        ContextCompat.getColor(this.context, enableId),
        ContextCompat.getColor(context, disableId))
    setTextColor(ColorStateList(status, colors))
}

internal fun TextView.applyEnabledColorIntStateList(@ColorInt enableId: Int,
                                                    @ColorInt disableId: Int) {
    val status: Array<IntArray> =
        arrayOf<IntArray>(intArrayOf(android.R.attr.state_enabled), intArrayOf())
    val colors: IntArray = intArrayOf(enableId, disableId)
    setTextColor(ColorStateList(status, colors))
}


internal fun TextView.applySelectedColorStateList(@ColorRes selectedId: Int,
                                                  @ColorRes unSelectedId: Int) {
    val status: Array<IntArray> =
        arrayOf<IntArray>(intArrayOf(android.R.attr.state_selected), intArrayOf())
    val colors: IntArray = intArrayOf(
        ContextCompat.getColor(this.context, selectedId),
        ContextCompat.getColor(context, unSelectedId))
    setTextColor(ColorStateList(status, colors))
}

internal fun TextView.applySelectedColorIntStateList(@ColorInt selectedId: Int,
                                                     @ColorInt unSelectedId: Int) {
    val status: Array<IntArray> =
        arrayOf<IntArray>(intArrayOf(android.R.attr.state_selected), intArrayOf())
    val colors: IntArray = intArrayOf(
        selectedId,
        unSelectedId)
    setTextColor(ColorStateList(status, colors))
}

fun FragmentManager.popBackStackNowAllowingStateLoss() {
    try {
        popBackStackImmediate()
    } catch (e: Exception) {}
}

fun AppCompatActivity.immersion() {
    ImmersionBar.with(this)
    .statusBarDarkFont(false)
    .fitsSystemWindows(false)
    .init()
}

/**
 * 防止多次点击
 */
inline fun View.setOnSingleClickListener(crossinline onClick: (view: View) -> Unit, delayMillis: Long) {
    this.setOnClickListener {
        if (this.isClickable) {
            this.isClickable = false
            onClick(it)
            this.postDelayed({
                this.isClickable = true
            }, delayMillis)
        }
    }
}

