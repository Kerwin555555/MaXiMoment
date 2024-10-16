package com.moment.app.utils

import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintLayout.LayoutParams
import androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.MATCH_CONSTRAINT
import androidx.constraintlayout.widget.ConstraintSet
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.blankj.utilcode.util.SizeUtils


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