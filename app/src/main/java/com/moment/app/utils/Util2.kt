package com.moment.app.utils

import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.util.LayoutDirection.RTL
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.gyf.immersionbar.ImmersionBar
import com.moment.app.R


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

internal fun View.isRTL() : Boolean  {
    return this.layoutDirection == RTL
}

internal fun View.resetGravity(g: Int) {
    if (this.layoutParams is FrameLayout.LayoutParams) {
        (this.layoutParams as FrameLayout.LayoutParams).gravity = g
    }
}

fun AppCompatImageView.loadImage(file: String?, sizeW: Int, sizeH: Int) {
    Glide.with(this)
        .setDefaultRequestOptions(RequestOptions.noAnimation().diskCacheStrategy(DiskCacheStrategy.RESOURCE))
        .load(R.mipmap.local_avatar)
        .override(sizeW, sizeH)
        .timeout(3000)
        .addListener(object : RequestListener<Drawable> {
            override fun onLoadFailed(
                e: GlideException?,
                model: Any?,
                target: Target<Drawable>,
                isFirstResource: Boolean
            ): Boolean {
                //dirtyFileAction?.invoke(file)
                return false
            }

            override fun onResourceReady(
                resource: Drawable,
                model: Any,
                target: Target<Drawable>?,
                dataSource: DataSource,
                isFirstResource: Boolean
            ): Boolean {
                return false
            }

        })
        .into(this)
}


const val MILLION = 1000000L

const val BILLION = 1000000000L

const val THOUSAND = 1000L
fun formatScore(calculator_score: Long): String {
    if (calculator_score >= MILLION) {
        val sb = StringBuilder()
        val g = calculator_score.toString().toCharArray()
        val length = g.size
        var needAddOne = false
        if (g[length - 4] >= '5') {
            needAddOne = true
        }
        if (needAddOne) {
            var plusone = 1
            for (i in length - 5 downTo 0) {
                g[i] = (g[i].code + plusone).toChar()
                if (g[i] > '9') {
                    g[i] = '0'
                    plusone = 1
                } else {
                    plusone = 0
                    break
                }
            }
            if (plusone == 1) {
                sb.append('1')
            }
            for (i in 0..length - 7) {
                sb.append(g[i])
            }
            sb.append('.')
            sb.append(g[length - 6])
            sb.append(g[length - 5])
            sb.append('M')
            return sb.toString()
        } else {
            for (i in 0..length - 7) {
                sb.append(g[i])
            }
            sb.append(".")
            sb.append(g[length - 6])
            sb.append(g[length - 5])
            sb.append('M')
            return sb.toString()
        }
    } else if (calculator_score >= THOUSAND) {
        val sb = StringBuilder()
        val g = calculator_score.toString().toCharArray()
        val length = g.size
        var needAddOne = false
        if (g[length - 1] >= '5') {
            needAddOne = true
        }
        if (needAddOne) {
            var plusone = 1
            for (i in length - 2 downTo 0) {
                g[i] = (g[i].code + plusone).toChar()
                if (g[i] > '9') {
                    g[i] = '0'
                    plusone = 1
                } else {
                    plusone = 0
                    break
                }
            }
            if (plusone == 1) {
                sb.append('1')
            }
            for (i in 0..length - 4) {
                sb.append(g[i])
            }
            sb.append('.')
            sb.append(g[length - 3])
            sb.append(g[length - 2])
            sb.append('K')
            return sb.toString()
        } else {
            for (i in 0..length - 4) {
                sb.append(g[i])
            }
            sb.append('.')
            sb.append(g[length - 3])
            sb.append(g[length - 2])
            sb.append('K')
            return sb.toString()
        }
    } else {
        return calculator_score.toString()
    }
}




