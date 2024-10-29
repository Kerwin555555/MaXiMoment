package com.moment.app.utils

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Bitmap
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
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.blankj.utilcode.util.LanguageUtils
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.ScreenUtils
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.google.common.collect.ImmutableList
import com.gyf.immersionbar.ImmersionBar
import com.moment.app.MomentApp
import com.moment.app.R
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.util.Locale


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

fun saveView(context: Context, bitmap: Bitmap): File? {
    val fileName = "moment_" + System.currentTimeMillis() + ".jpg"
    val pictureFile = File(context.cacheDir, fileName)

    try {
        val fos = FileOutputStream(pictureFile)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
        fos.flush()
        fos.close()
        return pictureFile
    } catch (e: FileNotFoundException) {
        LogUtils.d("ClipImage", "File not found: " + e.message)
        return null
    } catch (e: IOException) {
        LogUtils.d("ClipImage", "Error accessing file: " + e.message)
        return null
    } finally {
    }
}

fun AppCompatActivity.rightInRightOut() : FragmentTransaction{
    return this.supportFragmentManager
        .beginTransaction()
        .setCustomAnimations(
            R.anim.f_slide_in_right, // 进入动画
            0, // 退出动画（这里没有设置，所以为0）, // 退出动画（这里没有设置，所以为0）
            0, // 弹出动画（这里没有设置，所以为0）
            R.anim.f_slide_out_right ,  // 弹入动画（这里没有设置，所以为0）
        )
}

fun AppCompatActivity.bottomInBottomOut() : FragmentTransaction {
    return this.supportFragmentManager
        .beginTransaction()
        .setCustomAnimations(
            R.anim.slide_up, // 进入动画
            0, // 退出动画（这里没有设置，所以为0）, // 退出动画（这里没有设置，所以为0）
            0, // 弹出动画（这里没有设置，所以为0）
            R.anim.slide_down ,  // 弹入动画（这里没有设置，所以为0）
        )
}

fun AppCompatActivity.cleanSaveFragments() {
    kotlin.runCatching {
        val transaction = supportFragmentManager.beginTransaction()
        for (f in supportFragmentManager.fragments) {
            transaction.remove(f)
        }
        transaction.commitNow()
    }
}

fun Fragment.copyFragmentArgumentsToMap() : MutableMap<String, Any?>{
    val map = mutableMapOf<String, Any?>()
    arguments?.let {
        for (key in it.keySet()) {
            val value = it.get(key)
            map[key] = value
        }
    }
    return map
}

val LOCALE_IMMUTABLE_SET: ImmutableList<Locale> = ImmutableList.Builder<Locale>().add(
    Locale("en"),
    Locale("th"),
    Locale("vi"),
    Locale("in"),
    Locale("ms"),
    Locale("es"),
    Locale("pt"),
    Locale("tr"),
    Locale("ru"),
    Locale("ar"),
    Locale("ja"),
    Locale("zh")
).build()

fun getSelectedLoc(): Locale {
    var locale = LanguageUtils.getAppliedLanguage()
    if (locale == null) {
        locale = LanguageUtils.getSystemLanguage()
    }
    if (locale == null) return Locale.ENGLISH
    for (loc in LOCALE_IMMUTABLE_SET) {
        if (loc.language == locale.language) {
            return locale
        }
    }
    return Locale.ENGLISH
}

fun getScreenWidth() : Int{
    val w = ScreenUtils.getAppScreenWidth()
    if (w > 0) {
        return w
    }
    return MomentApp.appContext.resources.displayMetrics.widthPixels
}
fun getScreenHeight() : Int{
    val h = ScreenUtils.getAppScreenHeight()
    if (h > 0) {
        return h
    }
    return MomentApp.appContext.resources.displayMetrics.heightPixels
}




