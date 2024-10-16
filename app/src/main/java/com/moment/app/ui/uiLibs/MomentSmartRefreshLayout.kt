package com.moment.app.ui.uiLibs

import android.content.Context
import android.graphics.drawable.AnimationDrawable
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.ViewConfiguration
import android.widget.FrameLayout
import android.widget.Scroller
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.blankj.utilcode.util.SizeUtils
import com.blankj.utilcode.util.ToastUtils
import com.google.android.material.appbar.AppBarLayout
import com.moment.app.databinding.MomentRefreshLayoutBinding


class ZZSmartRefreshLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    companion object {
        val MAX_DRAG_DISTANCE = SizeUtils.dp2px(150f)
    }
    private val binding = MomentRefreshLayoutBinding.inflate(LayoutInflater.from(context), this)

    private var mMaxVelocity = 0
    private var mMinVelocity = 0

    private val mVelecityTracker: VelocityTracker
    private var isDragging = false
    private var scroller = Scroller(context)
    private val flingRunnable = FlingRunnable(0,  0)

    private var offset = 0

    init {
        val vg = ViewConfiguration.get(context)
        mMinVelocity = vg.scaledMinimumFlingVelocity
        mMaxVelocity = vg.scaledMaximumFlingVelocity
        mVelecityTracker = VelocityTracker.obtain()
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        binding.rv.layoutManager = LinearLayoutManager(context)
        binding.rv.adapter = DAdapter(context)
            .apply {
                runnable = Runnable {
                    //com.example.myapplication.dialogs.show(context, BaseDialog())
                }
            }
        binding.appbar.addOnOffsetChangedListener(object : AppBarLayout.OnOffsetChangedListener{
            override fun onOffsetChanged(appBarLayout: AppBarLayout?, verticalOffset: Int) {
                offset = verticalOffset
            }

        })
    }

    var downx = 0f
    var downy = 0f
    var distance = 0

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (flingRunnable.anim) {
            return false
        }
        mVelecityTracker.addMovement(ev)
        // ev.actionMasked multiple points  ev.action: single point touch
        when (ev.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                // 1. assure that Downevents is not prevented 2 consumed down events here
                downx = ev.y
                downy = ev.y
                distance = if (flingRunnable.type == 1) binding.coordinator.top else 0
                isDragging = false
                Log.d("zhouzheng", "down")
                if (flingRunnable.type != 1) {
                    super.dispatchTouchEvent(ev)
                }
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                // 如果可以move 就
                if (flingRunnable.type == 1) {
                    val detaly = (ev.y - downy).toInt()
                    if (detaly >= 0) {
                        var offset = detaly
                        if ((detaly + distance).toInt() >= SizeUtils.dp2px(60f)) {
                            offset = (SizeUtils.dp2px(60f) - distance)
                        }
                        distance += offset
                        ViewCompat.offsetTopAndBottom(binding.coordinator, offset)
                        ViewCompat.offsetTopAndBottom(binding.imageLoading, offset)
                    } else {
                        Log.d("distance旧的", "${detaly}AA${distance}")
                        var offset = detaly
                        if (distance + detaly <0) {
                            offset = -distance
                        }
                        distance += offset
                        Log.d("distance新的", "${distance}")
                        ViewCompat.offsetTopAndBottom(binding.coordinator, offset)
                        ViewCompat.offsetTopAndBottom(binding.imageLoading, offset)
                    }
                    downx = ev.y
                    downy = ev.y
                    return true
                }
                if (canDragContent(ev)) {
                    isDragging = true
                    // Log.d("zhouzheng", "isdrag 为true啦")
                } else {
                    isDragging = false
                }

                (binding.imageLoading.drawable as AnimationDrawable).selectDrawable(15)
                if (isDragging) {
                    val trueDelta = (ev.y - downy)  * dragRate(distance.toInt(), MAX_DRAG_DISTANCE)
                    Log.d("zhouzheng", "dragragte${dragRate(distance.toInt(), MAX_DRAG_DISTANCE)}")
                    var offset = trueDelta.toInt()
                    if (trueDelta + distance <= 0) {
                        offset = -distance.toInt()
                        //  Log.d("zhouzheng", "isdrag 为false啦")
                        isDragging = false
                    }
                    if (trueDelta + distance >= MAX_DRAG_DISTANCE) {
                        offset = (MAX_DRAG_DISTANCE - distance).toInt()
                    }
                    ViewCompat.offsetTopAndBottom(binding.coordinator, offset)
                    ViewCompat.offsetTopAndBottom(binding.imageLoading, offset)
                    distance += offset
                    downx = ev.y
                    downy = ev.y
                    return true
                }
                downx = ev.y
                downy = ev.y
            }
            MotionEvent.ACTION_UP -> {
                Log.d("zhouzheng", "up")
                if (isDragging) {
                    if (distance >= MAX_DRAG_DISTANCE * 2/3f) {
                        flingRunnable.apply {
                            anim = true
                            total = distance - SizeUtils.dp2px(60f)
                            Log.d("zhouzheng1", "total${distance}")
                            type = 1
                            postOnAnimation()
                            //开始绿色动画
                        }
                    } else {
                        flingRunnable.apply {
                            anim = true
                            type = 0
                            Log.d("zhouzheng1", "distance${distance}")
                            total = distance
                            postOnAnimation()
                        }
                        //mVelecityTracker.computeCurrentVelocity(1000, mMaxVelocity.toFloat())
//                    (context as AppCompatActivity).lifecycle.coroutineScope.launch {
//                       delay(2000)
//                       flingRunnable.postOnAnimation()
//                    }
                    }
                }
                isDragging = false
            }
            MotionEvent.ACTION_CANCEL -> {

            }
        }
        return super.dispatchTouchEvent(ev)
    }

    private fun goback() {
        flingRunnable.apply {
            anim = true
            type = 0
            Log.d("zhouzheng1", "distance${distance}")
            total = binding.coordinator.top
            postOnAnimation()
        }
    }

    // type 0 1. scroll distance 1. scroll to loadingt state, 2 from loading to top
    inner class FlingRunnable(var total: Int, var type: Int) : Runnable {
        var anim = false
        override fun run() {
            if (scroller.computeScrollOffset()) {
                // Log.d("zhouzheng", "手指放开动画啦${-scroller.currY} distrance${distance} detal${before - scroller.currY}")
                val delta = before - scroller.currY
                ViewCompat.offsetTopAndBottom(binding.coordinator, delta.toInt())
                ViewCompat.offsetTopAndBottom(binding.imageLoading, delta.toInt())
                before = scroller.currY
                this@ZZSmartRefreshLayout.postOnAnimation(this)
            } else {
                anim = false

                if (type == 1) {
                    (binding.imageLoading.drawable as AnimationDrawable).start()
                    binding.imageLoading.postDelayed({
                        (binding.imageLoading.drawable as AnimationDrawable).stop()
                        ToastUtils.showShort("OK downloading!")
                        goback()
                    }, 2000)
                }
            }
        }

        fun postOnAnimation() {
            anim = true
            before = total
            scroller.startScroll(0, total.toInt(), 0, total.toInt() ,150)
            this@ZZSmartRefreshLayout.postOnAnimation(this)
        }
        var before = 0
    }

    private fun dragRate(y: Int, total: Int) : Float{
        return Math.sqrt(1 - 1.0 *y/total).toFloat()
    }

    private fun canDragContent(event: MotionEvent): Boolean {
        return (distance > 0 || distance == 0 && (event.y - downy).toInt() > 0) && !canRvScroll() && !canAppbarScroll()
    }

    private fun canAppbarScroll(): Boolean {
//        Log.d("zhouzheng", "appbar offset${binding.appbar.top}H${offset}")
//        return offset < 0
        Log.d("zhouzheng", "appbar offset${binding.appbar.top}")
        return binding.appbar.top < 0
    }

    private fun canRvScroll(): Boolean {
        //  Log.d("zhouzheng", "recyclervierw 能下滑吗${binding.rv.canScrollVertically(-1)}")
        return binding.rv.canScrollVertically(-1)
    }
}