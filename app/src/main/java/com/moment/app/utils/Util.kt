package com.moment.app.utils

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintLayout.LayoutParams
import androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.MATCH_CONSTRAINT
import androidx.constraintlayout.widget.ConstraintSet
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import com.blankj.utilcode.util.BarUtils
import com.blankj.utilcode.util.SizeUtils
import com.blankj.utilcode.util.ToastUtils
import com.moment.app.entities.UserInfo
import com.moment.app.main_home.subfragments.entities.UserInfoList
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import retrofit2.HttpException
import java.io.IOException
import java.net.ConnectException
import kotlin.coroutines.cancellation.CancellationException
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
    start: Float? = null,
    top: Float? = null,
    end: Float? = null,
    bottom: Float? = null
) {
    setPaddingRelative(
        if(start == null) 0 else SizeUtils.dp2px(start),
        if(top == null) 0 else SizeUtils.dp2px(top),
        if(end == null) 0 else SizeUtils.dp2px(end),
        if(bottom == null) 0 else SizeUtils.dp2px(bottom),
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
        f.user_infos?.add(UserInfo("${start_pos + i}", if (i%2==0) "male" else "female",19 ))
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
        f.user_infos?.add(UserInfo("${start_pos + i}", if (i%2==0) "male" else "female",19 ))
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

