package com.moment.app.utils

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout

class SlideDrawerLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    private lateinit var mViewDragHelper: MomentViewDragHelper

    var onMoveToEnd : (() -> Unit)? = null


    var targetView: View? = null
    var click: View? = null
    private var CONTENT_WIDTH: Int = 0
    private var START: Int = 0
    private var END: Int = 0

    fun initContent(view: View, block: (View) -> Unit, onMoveToEnd: (() -> Unit)?, ) {
        targetView = view
        this.onMoveToEnd = onMoveToEnd
        view.layoutParams = LayoutParams(CONTENT_WIDTH, MATCH_PARENT).apply {
            gravity = Gravity.END
        }
        addView(view)
        click = View(context).apply {
            layoutParams = LayoutParams(START, MATCH_PARENT).apply {
                gravity = Gravity.START
            }
        }
        addView(click)
        click?.clicks(block = block)
    }

    init {
        CONTENT_WIDTH =  (context.resources.displayMetrics.widthPixels * 5f/6f).toInt()
        START = CONTENT_WIDTH/5
        END = context.resources.displayMetrics.widthPixels
        mViewDragHelper = MomentViewDragHelper.create(this, 1.0f, object : MomentViewDragHelper.Callback() {
            override fun tryCaptureView(child: View, pointerId: Int): Boolean {
                return child == targetView
            }

            override fun clampViewPositionHorizontal(child: View?, left: Int, dx: Int): Int {
                return if (left > START) {
                    left
                } else START
            }

            override fun getViewHorizontalDragRange(child: View): Int {
                return 1
            }


            override fun getViewVerticalDragRange(child: View): Int {
                return 1
            }

            override fun onViewReleased(releasedChild: View?, xvel: Float, yvel: Float) {
                super.onViewReleased(releasedChild, xvel, yvel)
                //手指抬起后缓慢移动到指定位置
                targetView?.let {
                    if (it.left - START < CONTENT_WIDTH/3) {
                        mViewDragHelper.smoothSlideViewTo(it ,START, top)
                    } else {
                        mViewDragHelper.smoothSlideViewTo(it, END, top)
                    }
                }
                invalidate()
            }
        })
        mViewDragHelper.setDuration(100)
    }

    /**
     * 重写事件拦截方法，将事件传给ViewDragHelper进行处理
     *
     * @param ev
     * @return
     */
    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        return mViewDragHelper.shouldInterceptTouchEvent(ev!!)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        mViewDragHelper.processTouchEvent(event!!)
        return true
    }

    override fun computeScroll() {
        super.computeScroll()
        if (mViewDragHelper.continueSettling(false)) {
            invalidate()
        } else if (targetView?.left == END) {
            this.onMoveToEnd?.invoke()
        }
    }
}