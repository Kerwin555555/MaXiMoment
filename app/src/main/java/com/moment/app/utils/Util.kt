package com.moment.app.utils

import android.R
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.BitmapFactory
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.StateListDrawable
import android.util.Log
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintLayout.LayoutParams
import androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.MATCH_CONSTRAINT
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.blankj.utilcode.util.BarUtils
import com.blankj.utilcode.util.SizeUtils
import com.blankj.utilcode.util.ToastUtils
import com.moment.app.datamodel.UserInfo
import com.moment.app.main_home.subfragments.models.UserInfoList
import com.moment.app.network.UserCancelException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.suspendCancellableCoroutine
import org.w3c.dom.Text
import java.util.UUID
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.resume


inline val Int.dp: Int get() = SizeUtils.dp2px(this.toFloat())

//fun Int.dp() : Int {
//    return SizeUtils.dp2px(this.toFloat())
//}

fun Float.dp() : Int {
    return SizeUtils.dp2px(this)
}


/**
 * LiveData并非在注册观察者后LifecycleOwner第一次生命周期变化时才运行onChange‌。
 * 实际上，LiveData的运行机制是基于LifecycleOwner的当前生命周期状态。
 * 当观察者注册到LiveData时，如果LifecycleOwner已经处于STARTED或RESUMED状态，
 * LiveData会立即认为自己是活跃的，并可以开始分发数据，此时onChange方法会被调用。
 * 如果LifecycleOwner的状态是CREATED或更早的状态，那么LiveData会等待LifecycleOwner的状态变
 * 为STARTED或RESUMED后才认为自己是活跃的，随后才会调用onChange方法分发数据
 */
class StickyLiveData<T> : MutableLiveData<T>() {
    private var stickyData: T? = null

    override fun setValue(value: T?) {
        stickyData = value
        super.setValue(value)
    }

    override fun observe(owner: LifecycleOwner, observer: Observer<in T>) {
        // 如果stickyData不为null，说明有粘性事件数据，先发送给观察者
        stickyData?.let { observer.onChanged(it) }
        // 然后调用super.observe注册观察者，以便后续数据更新时能够接收到通知
        super.observe(owner, observer)
    }
}


//Programatically build a horizontal chain
fun ConstraintLayout.buildHorizontalChain(buttonIds: IntArray) {
    val numberOfNaviTabs = buttonIds.size
    for (i in 0 until numberOfNaviTabs) {
        val tabView = getChildAt(i)
        tabView.layoutParams = LayoutParams(MATCH_CONSTRAINT, ViewGroup.LayoutParams.MATCH_PARENT)
    }
    val constraintSet = ConstraintSet()
    constraintSet.clone(this) // 复制现有约束
    for (buttonId in buttonIds) {
        constraintSet.connect(
            buttonId,
            ConstraintSet.TOP,
            ConstraintSet.PARENT_ID,
            ConstraintSet.TOP,
            0
        )
        constraintSet.connect(
            buttonId,
            ConstraintSet.BOTTOM,
            ConstraintSet.PARENT_ID,
            ConstraintSet.BOTTOM,
            0
        )
    }
    val array = FloatArray(numberOfNaviTabs) { 1f } // 所有按钮的权重相同
    constraintSet.createHorizontalChain(
        ConstraintSet.PARENT_ID,
        ConstraintSet.LEFT,
        ConstraintSet.PARENT_ID,
        ConstraintSet.RIGHT,
        buttonIds,
        array,
        ConstraintLayout.LayoutParams.CHAIN_SPREAD_INSIDE
    )
    constraintSet.applyTo(this)
}



internal val View?.localVisibleRect: Rect
    get() = Rect().also { this?.getLocalVisibleRect(it) }

internal val View?.globalVisibleRect: Rect
    get() = Rect().also { this?.getGlobalVisibleRect(it) }

internal val View?.hitRect: Rect
    get() = Rect().also { this?.getHitRect(it) }

internal val View?.isRectVisible: Boolean
    get() = this != null && globalVisibleRect != localVisibleRect

internal fun View.makeVisible() {
    visibility = View.VISIBLE
}

internal fun View.makeInvisible() {
    visibility = View.INVISIBLE
}

internal fun View.makeGone() {
    visibility = View.GONE
}

internal inline fun <T : View> T.postApply(crossinline block: T.() -> Unit) {
    post { apply(block) }
}

internal inline fun <T : View> T.postDelayed(delayMillis: Long, crossinline block: T.() -> Unit) {
    postDelayed({ block() }, delayMillis)
}

internal fun View.applyMargin(
    start: Int? = null,
    top: Int? = null,
    end: Int? = null,
    bottom: Int? = null
) {
    if (layoutParams is ViewGroup.MarginLayoutParams) {
        layoutParams = (layoutParams as ViewGroup.MarginLayoutParams).apply {
            marginStart = start ?: marginStart
            topMargin = top ?: topMargin
            marginEnd = end ?: marginEnd
            bottomMargin = bottom ?: bottomMargin
        }
    }
}

internal fun View.applyMarginLTR(
    left: Int? = null,
    top: Int? = null,
    right: Int? = null,
    bottom: Int? = null
) {
    if (layoutParams is ViewGroup.MarginLayoutParams) {
        layoutParams = (layoutParams as ViewGroup.MarginLayoutParams).apply {
            leftMargin = left ?:  leftMargin
            topMargin = top ?: topMargin
            rightMargin = right ?: rightMargin
            bottomMargin = bottom ?: bottomMargin
        }
    }
}

//默认 0
internal fun View.applyPaddingsWithDefaultZero(
    start: Int? = null,
    top: Int? = null,
    end: Int? = null,
    bottom: Int? = null
) {
    setPaddingRelative(
        if(start == null) 0 else start,
        if(top == null) 0 else top,
        if(end == null) 0 else end,
        if(bottom == null) 0 else bottom,
    )
}


internal fun View.requestNewSize(
    width: Int, height: Int) {
    layoutParams.width = width
    layoutParams.height = height
    layoutParams = layoutParams
}

internal fun View.makeViewMatchParent() {
    applyMargin(0, 0, 0, 0)
    requestNewSize(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.MATCH_PARENT)
}


internal fun TextView.applyDrawable(
    start: Int? = null,
    top: Int? = null,
    end: Int? = null,
    bottom: Int? = null
) {
    setCompoundDrawablesRelativeWithIntrinsicBounds(start ?: 0, top ?: 0, end ?: 0, bottom ?: 0)
}

internal fun TextView.applyDrawableInstance(
    start: Drawable? = null,
    top: Drawable? = null,
    end: Drawable? = null,
    bottom: Drawable? = null
) {
    setCompoundDrawablesRelativeWithIntrinsicBounds(start, top, end, bottom)
}

internal fun View.animateAlpha(from: Float?, to: Float?, duration: Long) {
    alpha = from ?: 0f
    clearAnimation()
    animate()
        .alpha(to ?: 0f)
        .setDuration(duration)
        .start()
}

internal fun View.animateAlpha(from: Float?, to: Float?, duration: Long, onAnimationEnd: ((animation: Animator?) -> Unit)? = null) {
    alpha = from ?: 0f
    clearAnimation()
    animate()
        .alpha(to ?: 0f)
        .setListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {}
            override fun onAnimationEnd(animation: Animator) {
                kotlin.runCatching {
                    onAnimationEnd?.invoke(animation)
                }.onFailure {
                    it.printStackTrace()
                }
            }
            override fun onAnimationCancel(animation: Animator) {}
            override fun onAnimationRepeat(animation: Animator) {}
        })
        .setDuration(duration)
        .start()
}

suspend fun View.alphaAnimate(from: Float?, to: Float?, duration: Long): Animator {
    return suspendCancellableCoroutine {it ->
        alpha = from ?: 0f
        clearAnimation()
        animate()
            .alpha(to ?: 0f)
            .setListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator) {}
                override fun onAnimationEnd(animation: Animator) {
                    it.resume(animation)
                }
                override fun onAnimationCancel(animation: Animator) {
                }
                override fun onAnimationRepeat(animation: Animator) {}
            })
            .setDuration(duration)
            .start()
    }
}

internal fun View.animateTranslationY(duration: Long) {
    animate()
        .translationY(0f)
        .setDuration(duration)
        .start()
}

internal fun View.animateScale(duration: Long, onAnimationEnd: ((animation: Animator?) -> Unit)? = null) {
    animate()
        .scaleY(0f)
        .scaleX(0f)
        .setListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {}
            override fun onAnimationEnd(animation: Animator) {
                kotlin.runCatching {
                    onAnimationEnd?.invoke(animation)
                }.onFailure {
                    it.printStackTrace()
                }
            }
            override fun onAnimationCancel(animation: Animator) {}
            override fun onAnimationRepeat(animation: Animator) {}
        })
        .setDuration(duration)
        .start()
}

internal fun View.animateTranslationY(duration: Long, translationY: Float, onAnimationEnd: ((animation: Animator?) -> Unit)? = null) {
    animate()
        .translationY(translationY)
        .setListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {}
            override fun onAnimationEnd(animation: Animator) {
                onAnimationEnd?.invoke(animation)
            }
            override fun onAnimationCancel(animation: Animator) {}
            override fun onAnimationRepeat(animation: Animator) {}
        })
        .setDuration(duration)
        .start()
}

internal fun View.animateTranslationYCallback(duration: Long, onAnimationEnd: ((animation: Animator?) -> Unit)? = null) {
    animate()
        .translationY(0f)
        .setListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {}
            override fun onAnimationEnd(animation: Animator) {
                onAnimationEnd?.invoke(animation)
            }
            override fun onAnimationCancel(animation: Animator) {}
            override fun onAnimationRepeat(animation: Animator) {}
        })
        .setDuration(duration)
        .start()
}

internal fun View.switchVisibilityWithAnimation() {
    val isVisible = visibility == View.VISIBLE
    val from = if (isVisible) 1.0f else 0.0f
    val to = if (isVisible) 0.0f else 1.0f

    ObjectAnimator.ofFloat(this, "alpha", from, to).apply {
        duration = ViewConfiguration.getDoubleTapTimeout().toLong()

        if (isVisible) {
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    makeGone()
                }
            })
        } else {
            makeVisible()
        }

        start()
    }
}

fun BaseActivity.getStatusBarHeight(): Int{
    return BarUtils.getStatusBarHeight()
}

fun BaseFragment.getStatusBarHeight(): Int{
    return BarUtils.getStatusBarHeight()
}

suspend fun generateMockUserInfos(start_pos : Int = 0, limit: Int =10) : UserInfoList{
    val f = UserInfoList()
    var noNext = false
    f.user_infos = mutableListOf()
    for (i in 0 until 10) {
        if (start_pos + i > 50) noNext = true
        f.user_infos?.add(UserInfo("${start_pos + i}", if (i%2==0) "male" else "female",age = 19 ))
    }
    delay(1200)
    f.has_next = !noNext
    f.next_start = start_pos + 10
    return f
}

suspend fun generateEmptyMockUserInfos(start_pos : Int = 0, limit: Int =10) : UserInfoList{
    val f = UserInfoList()
    var noNext = false
    f.user_infos = mutableListOf()
    for (i in 0 until 10) {
        if (start_pos + i > 50) noNext = true
        val uuid = UUID.randomUUID()
        f.user_infos?.add(UserInfo("${start_pos + i}", if (i%2==0) "male" else "female",age = 19 ).apply {
            name = "Moment"+uuid
        })
    }
    delay(1200)
    f.has_next = !noNext
    f.next_start = start_pos + 10
    return UserInfoList().apply { user_infos = mutableListOf()
    has_next=false}
}

suspend fun generateErrorMockUserInfos(start_pos : Int = 0, limit: Int =10) : UserInfoList{

    delay(1200)
    throw RuntimeException("api error")
    return UserInfoList().apply { user_infos = mutableListOf()
        has_next=false}
}


fun String.toast() {
    if (this.isNullOrEmpty() || ActivityHolder.getCurrentActivity() == null) {
        return
    }
    ToastUtils.showShort(this)
}

//取代 deprecated 的 GlobalCope
val coroutineScope = CoroutineScope(EmptyCoroutineContext)

//比如全局子线程切主线程可以直接用该scope
val coroutineScope2 = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

fun Job.cancelIfActive() {
    if (this.isActive) {
        this.cancel(UserCancelException())
    }
}

fun printStack(start: String) {
    val elements = Throwable().stackTrace
    val builder = StringBuilder()
    for (i in elements.indices) {
        if (i < 34) {
            val element = elements[i]
            builder.append(element.className).append(".").append(element.methodName).append("\n")
        }
    }
    Log.d("zhouzheng", start + builder.toString())
}

internal fun View.setBgStateListDrawable(
    dp: Float,
    @ColorRes enableId: Int,
    @ColorRes disableId: Int,
    @ColorRes pressedId: Int? = null
) {
    setBgColorIntStateListDrawable(dp, ContextCompat.getColor(this.context, enableId),
        ContextCompat.getColor(this.context, disableId), if (pressedId != null)
            ContextCompat.getColor(this.context, pressedId) else null)
}

internal fun View.setBgColorIntStateListDrawable(
    dp: Float,
    @ColorInt enableId: Int,
    @ColorInt disableId: Int,
    @ColorInt pressedId: Int? = null
) {
    val drawable = StateListDrawable()
    drawable.addState(
        intArrayOf(android.R.attr.state_enabled),
        GradientDrawableBuilder()
            .conner(SizeUtils.dp2px(dp))
            .color(enableId)
            .build()
    )
    drawable.addState(
        intArrayOf(), GradientDrawableBuilder()
            .conner(SizeUtils.dp2px(dp))
            .color(disableId)
            .build()
    )
    if (pressedId != null) {
        drawable.addState(
            intArrayOf(android.R.attr.state_pressed),
            GradientDrawableBuilder()
                .conner(SizeUtils.dp2px(dp))
                .color(pressedId)
                .build()
        )
    }
    background = drawable
}

internal fun View.setBgSelectedStateListDrawable(
    dp: Float,
    @ColorRes selectedId: Int,
    @ColorRes unSelectedId: Int
) {
    setBgSelectedColorIntStateListDrawable(dp, ContextCompat.getColor(this.context, selectedId),
        ContextCompat.getColor(this.context, unSelectedId))
}

internal fun View.setBgSelectedColorIntStateListDrawable(
    dp: Float,
    @ColorInt selectedId: Int,
    @ColorInt unSelectedId: Int
) {
    val drawable = StateListDrawable()
    drawable.addState(
        intArrayOf(android.R.attr.state_selected),
        GradientDrawableBuilder()
            .conner(SizeUtils.dp2px(dp))
            .color(selectedId)
            .build()
    )
    drawable.addState(
        intArrayOf(), GradientDrawableBuilder()
            .conner(SizeUtils.dp2px(dp))
            .color(unSelectedId)
            .build()
    )
    background = drawable
}

internal fun View.setBgWithAllCorners(dp: Float, @ColorRes solid: Int) {
    background = GradientDrawableBuilder()
        .conner(SizeUtils.dp2px(dp))
        .color(ContextCompat.getColor(this.context, solid))
        .build()
}

internal fun View.setBgWithCornerRadiusAndColor(dp: Float, @ColorInt solid: Int) {
    background = GradientDrawableBuilder()
        .conner(SizeUtils.dp2px(dp))
        .color(solid)
        .build()
}

internal fun ImageView.setImageResourceSelectedStateListDrawable(
    @DrawableRes selectedId:  Int,
    @DrawableRes unSelectedId: Int
) {
    val drawable = StateListDrawable()
    drawable.addState(
        intArrayOf(R.attr.state_selected),
        ContextCompat.getDrawable(context, selectedId)
    )
    drawable.addState(
        intArrayOf(),
        ContextCompat.getDrawable(context, unSelectedId)
    )
    setImageDrawable(drawable)
}

internal fun TextView.setTextColorResStateSelectList(
    @ColorRes selectedId: Int,
    @ColorRes unSelectedId: Int
) {
    val list = ColorStateList(
        arrayOf(intArrayOf(R.attr.state_selected), intArrayOf()),
        intArrayOf(ContextCompat.getColor(context, selectedId),
            ContextCompat.getColor(context, unSelectedId))
    )
    setTextColor(list)
}

internal fun TextView.setTextColorStateSelectList(
    @ColorInt selectedColor: Int,
    @ColorInt unSelectedColor: Int
) {
    val list = ColorStateList(
       arrayOf(intArrayOf(R.attr.state_selected), intArrayOf()),
        intArrayOf(selectedColor, unSelectedColor)
    )
    setTextColor(list)
}

internal fun View.setBgWithTopLeftRightCorners(@ColorRes solid: Int) {
    val size = SizeUtils.dp2px(20f).toFloat()
    val drawable = GradientDrawableBuilder()
        .conners(floatArrayOf(size, size, size, size, 0f, 0f, 0f, 0f))
        .color(ContextCompat.getColor(this.context, solid))
        .build()
    background = drawable
}

internal fun View.setBgWithBottomLeftRightCorners(@ColorRes solid: Int) {
    val size = SizeUtils.dp2px(20f).toFloat()
    val drawable = GradientDrawableBuilder()
        .conners(floatArrayOf(0f, 0f, 0f, 0f, size, size, size, size))
        .color(ContextCompat.getColor(this.context, solid))
        .build()
    background = drawable
}


//IMAGEVIEW

internal fun ImageView.setImageResourceEnableStateListDrawable(
    enable: Drawable,
    disable: Drawable
) {
    val drawable = StateListDrawable()
    drawable.addState(
        intArrayOf(android.R.attr.state_enabled),
        enable
    )
    drawable.addState(
        intArrayOf(),
        disable
    )
    setImageDrawable(drawable)
}

internal fun ImageView.setImageResourceEnableStateListDrawable(disableColorInt: Int, enableColorInt: Int, bitmapRes: Int) {
    setImageResourceEnableStateListDrawable(enable = BitmapDrawable(null,
        BitmapFactory.decodeResource(context.resources, bitmapRes)).apply {
        setTint(enableColorInt)
    },
        disable = BitmapDrawable(null, BitmapFactory.decodeResource(context.resources, bitmapRes)).apply {
            setTint(disableColorInt)
        } )
}


fun getStateListDrawableWithBitmaps(context: Context, stateResourceId: Int, stateBitmapId: Int, normalBitmapId: Int) : StateListDrawable {
    return StateListDrawable().apply {
        addState(intArrayOf(stateResourceId),  BitmapDrawable(context.resources, BitmapFactory.decodeResource(context.resources, stateBitmapId)))
        addState(intArrayOf(),  BitmapDrawable(context.resources, BitmapFactory.decodeResource(context.resources, normalBitmapId)))
    }
}

class GradientDrawableBuilder {
    private var conner: Int? = null
    private var conners: FloatArray? = null
    private var color: Int? = null
    private var colors: IntArray? = null
    private var orientation: GradientDrawable.Orientation? = null
    private var strokeColor: Int? = null
    private var strokeWidth: Int? = null

    fun build(): GradientDrawable {
        val gd = GradientDrawable()
        if (this.conner != null) {
            gd.cornerRadius = conner!!.toFloat()
        }

        if (this.conners != null) {
            gd.cornerRadii = conners
        }

        if (this.color != null) {
            gd.setColor(color!!)
        }

        if (this.colors != null) {
            gd.colors = colors
        }

        if (this.orientation != null) {
            gd.orientation = orientation
        }

        if (this.strokeWidth != null && this.strokeColor != null) {
            gd.setStroke(strokeWidth!!, strokeColor!!)
        }

        return gd
    }

    fun into(view: View) {
        val gd = GradientDrawable()
        if (this.conner != null) {
            gd.cornerRadius = conner!!.toFloat()
        }

        if (this.conners != null) {
            gd.cornerRadii = conners
        }

        if (this.color != null) {
            gd.setColor(color!!)
        }

        if (this.colors != null) {
            gd.colors = colors
        }

        if (this.orientation != null) {
            gd.orientation = orientation
        }

        if (this.strokeWidth != null && this.strokeColor != null) {
            gd.setStroke(strokeWidth!!, strokeColor!!)
        }

        view.background = gd
    }

    fun conner(conner: Int): GradientDrawableBuilder {
        this.conner = conner
        return this
    }

    fun conners(conners: FloatArray?): GradientDrawableBuilder {
        this.conners = conners
        return this
    }

    fun color(color: Int): GradientDrawableBuilder {
        this.color = color
        return this
    }

    fun colors(colors: IntArray?): GradientDrawableBuilder {
        this.colors = colors
        return this
    }

    fun strokeColor(strokeColor: Int): GradientDrawableBuilder {
        this.strokeColor = strokeColor
        return this
    }

    fun strokeWidth(strokeWidth: Int): GradientDrawableBuilder {
        this.strokeWidth = strokeWidth
        return this
    }

    fun orientation(orientation: GradientDrawable.Orientation?): GradientDrawableBuilder {
        this.orientation = orientation
        return this
    }
}

