package com.moment.app.utils

import android.content.Context
import android.util.Log
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup
import android.view.animation.Interpolator
import android.view.animation.LinearInterpolator
import android.widget.Scroller
import androidx.annotation.Px
import androidx.core.view.ViewCompat
import java.util.Arrays
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.sin

/**
 * 根据ViewDragHelper改进，原来sdk中无法设置duration,这里添加 [.setDuration]
 */
class MomentViewDragHelper private constructor(
    context: Context, forParent: ViewGroup,
    cb: MomentViewDragHelper.Callback
) {
    var viewDragState: Int = 0
        private set

    @get:Px
    var touchSlop: Int
         set

    var activePointerId: Int = INVALID_POINTER
        private set
    private var mInitialMotionX: FloatArray?= null
    private var mInitialMotionY: FloatArray? = null
    private var mLastMotionX: FloatArray? = null
    private var mLastMotionY: FloatArray? = null
    private var mInitialEdgesTouched: IntArray? = null
    private var mEdgeDragsInProgress: IntArray?= null
    private var mEdgeDragsLocked: IntArray? = null
    private var mPointersDown = 0

    private var mVelocityTracker: VelocityTracker? = null
    private val mMaxVelocity: Float

    var minVelocity: Float
    @get:Px
    val edgeSize: Int
    private var mTrackingEdges = 0

    private val mScroller: Scroller

    private val mCallback: Callback

    /**
     * @return The currently captured view, or null if no view has been captured.
     */
    var capturedView: View? = null
        private set
    private var mReleaseInProgress = false

    private val mParentView: ViewGroup

    private var duration = 100

    abstract class Callback {
        open fun onViewDragStateChanged(state: Int) {}

        open fun onViewPositionChanged(
            changedView: View?, left: Int, top: Int, @Px dx: Int,
            @Px dy: Int
        ) {
        }

        open fun onViewCaptured(capturedChild: View, activePointerId: Int) {}

        open fun onViewReleased(releasedChild: View?, xvel: Float, yvel: Float) {}

        open fun onEdgeTouched(edgeFlags: Int, pointerId: Int) {}

        open fun onEdgeLock(edgeFlags: Int): Boolean {
            return false
        }

        open fun onEdgeDragStarted(edgeFlags: Int, pointerId: Int) {}

        open  fun getOrderedChildIndex(index: Int): Int {
            return index
        }

        open fun getViewHorizontalDragRange(child: View): Int {
            return 0
        }

        open fun getViewVerticalDragRange(child: View): Int {
            return 0
        }

        open abstract fun tryCaptureView(child: View, pointerId: Int): Boolean

        open fun clampViewPositionHorizontal(child: View?, left: Int, dx: Int): Int {
            return 0
        }

        open fun clampViewPositionVertical(child: View?, top: Int, dy: Int): Int {
            return 0
        }
    }

    private val mSetIdleRunnable: Runnable = Runnable { setDragState(STATE_IDLE) }

    init {
        if (forParent == null) {
            throw IllegalArgumentException("Parent view may not be null")
        }
        if (cb == null) {
            throw IllegalArgumentException("Callback may not be null")
        }

        mParentView = forParent
        mCallback = cb

        val vc = ViewConfiguration.get(context)
        val density = context.resources.displayMetrics.density
        edgeSize = (EDGE_SIZE * density + 0.5f).toInt()

        touchSlop = vc.scaledTouchSlop
        mMaxVelocity = vc.scaledMaximumFlingVelocity.toFloat()
        minVelocity = vc.scaledMinimumFlingVelocity.toFloat()
        mScroller = Scroller(context, sInterpolator)
    }

    fun setEdgeTrackingEnabled(edgeFlags: Int) {
        mTrackingEdges = edgeFlags
    }

    fun captureChildView(childView: View, activePointerId: Int) {
        if (childView.parent !== mParentView) {
            throw IllegalArgumentException(
                "captureChildView: parameter must be a descendant "
                        + "of the ViewDragHelper's tracked parent view (" + mParentView + ")"
            )
        }

        capturedView = childView
        this.activePointerId = activePointerId
        mCallback.onViewCaptured(childView, activePointerId)
        setDragState(STATE_DRAGGING)
    }

    fun cancel() {
        activePointerId = INVALID_POINTER
        clearMotionHistory()

        if (mVelocityTracker != null) {
            mVelocityTracker!!.recycle()
            mVelocityTracker = null
        }
    }

    /**
     * [.cancel], but also abort all motion in progress and snap to the end of any
     * animation.
     */
    fun abort() {
        cancel()
        if (viewDragState == STATE_SETTLING) {
            val oldX = mScroller.currX
            val oldY = mScroller.currY
            mScroller.abortAnimation()
            val newX = mScroller.currX
            val newY = mScroller.currY
            mCallback.onViewPositionChanged(capturedView, newX, newY, newX - oldX, newY - oldY)
        }
        setDragState(STATE_IDLE)
    }

    fun smoothSlideViewTo(child: View, finalLeft: Int, finalTop: Int): Boolean {
        capturedView = child
        activePointerId = INVALID_POINTER

        val continueSliding = forceSettleCapturedViewAt(finalLeft, finalTop, 0, 0)
        if (!continueSliding && (viewDragState == STATE_IDLE) && (capturedView != null)) {
            // If we're in an IDLE state to begin with and aren't moving anywhere, we
            // end up having a non-null capturedView with an IDLE dragState
            capturedView = null
        }

        return continueSliding
    }

    fun settleCapturedViewAt(finalLeft: Int, finalTop: Int): Boolean {
        if (!mReleaseInProgress) {
            throw IllegalStateException(
                ("Cannot settleCapturedViewAt outside of a call to "
                        + "Callback#onViewReleased")
            )
        }

        return forceSettleCapturedViewAt(
            finalLeft, finalTop,
            mVelocityTracker!!.getXVelocity(activePointerId).toInt(),
            mVelocityTracker!!.getYVelocity(activePointerId).toInt()
        )
    }

    private fun forceSettleCapturedViewAt(
        finalLeft: Int,
        finalTop: Int,
        xvel: Int,
        yvel: Int
    ): Boolean {
        val startLeft = capturedView!!.left
        val startTop = capturedView!!.top
        val dx = finalLeft - startLeft
        val dy = finalTop - startTop

        if (dx == 0 && dy == 0) {
            // Nothing to do. Send callbacks, be done.
            mScroller.abortAnimation()
            setDragState(STATE_IDLE)
            return false
        }

        val duration = this.duration
        mScroller.startScroll(startLeft, startTop, dx, dy, duration)

        setDragState(STATE_SETTLING)
        return true
    }

    fun setDuration(d: Int) {
        this.duration = d
    }

    private fun computeAxisDuration(delta: Int, velocity: Int, motionRange: Int): Int {
        var velocity = velocity
        if (delta == 0) {
            return 0
        }

        val width = mParentView.width
        val halfWidth = width / 2
        val distanceRatio =
            min(1.0, (abs(delta.toDouble()).toFloat() / width).toDouble()).toFloat()
        val distance = halfWidth + (halfWidth
                * distanceInfluenceForSnapDuration(distanceRatio))

        val duration: Int
        velocity = abs(velocity.toDouble()).toInt()
        if (velocity > 0) {
            duration = (4 * Math.round(1000 * abs((distance / velocity).toDouble()))).toInt()
        } else {
            val range = abs(delta.toDouble()).toFloat() / motionRange
            duration = ((range + 1) * BASE_SETTLE_DURATION).toInt()
        }
        return min(duration.toDouble(), MAX_SETTLE_DURATION.toDouble())
            .toInt()
    }


    private fun clampMag(value: Int, absMin: Int, absMax: Int): Int {
        val absValue = abs(value.toDouble()).toInt()
        if (absValue < absMin) return 0
        if (absValue > absMax) return if (value > 0) absMax else -absMax
        return value
    }


    private fun clampMag(value: Float, absMin: Float, absMax: Float): Float {
        val absValue = abs(value.toDouble()).toFloat()
        if (absValue < absMin) return 0f
        if (absValue > absMax) return if (value > 0) absMax else -absMax
        return value
    }

    private fun distanceInfluenceForSnapDuration(f: Float): Float {
        var f = f
        f -= 0.5f // center the values about 0.
        f *= 0.3f * Math.PI.toFloat() / 2.0f
        return sin(f.toDouble()).toFloat()
    }

    fun flingCapturedView(minLeft: Int, minTop: Int, maxLeft: Int, maxTop: Int) {
        if (!mReleaseInProgress) {
            throw IllegalStateException(
                ("Cannot flingCapturedView outside of a call to "
                        + "Callback#onViewReleased")
            )
        }

        mScroller.fling(
            capturedView!!.left, capturedView!!.top,
            mVelocityTracker!!.getXVelocity(activePointerId).toInt(),
            mVelocityTracker!!.getYVelocity(activePointerId).toInt(),
            minLeft, maxLeft, minTop, maxTop
        )

        setDragState(STATE_SETTLING)
    }


    fun continueSettling(deferCallbacks: Boolean): Boolean {
        if (viewDragState == STATE_SETTLING) {
            var keepGoing = mScroller.computeScrollOffset()
            val x = mScroller.currX
            val y = mScroller.currY
            val dx = x - capturedView!!.left
            val dy = y - capturedView!!.top

            if (dx != 0) {
                ViewCompat.offsetLeftAndRight((capturedView)!!, dx)
            }
            if (dy != 0) {
                ViewCompat.offsetTopAndBottom((capturedView)!!, dy)
            }

            if (dx != 0 || dy != 0) {
                mCallback.onViewPositionChanged(capturedView, x, y, dx, dy)
            }

            if (keepGoing && (x == mScroller.finalX) && (y == mScroller.finalY)) {
                // Close enough. The interpolator/scroller might think we're still moving
                // but the user sure doesn't.
                mScroller.abortAnimation()
                keepGoing = false
            }

            if (!keepGoing) {
                if (deferCallbacks) {
                    mParentView.post(mSetIdleRunnable)
                } else {
                    setDragState(STATE_IDLE)
                }
            }
        }

        return viewDragState == STATE_SETTLING
    }

    /**
     * Like all callback events this must happen on the UI thread, but release
     * involves some extra semantics. During a release (mReleaseInProgress)
     * is the only time it is valid to call [.settleCapturedViewAt]
     * or [.flingCapturedView].
     */
    private fun dispatchViewReleased(xvel: Float, yvel: Float) {
        mReleaseInProgress = true
        mCallback.onViewReleased(capturedView, xvel, yvel)
        mReleaseInProgress = false

        if (viewDragState == STATE_DRAGGING) {
            // onViewReleased didn't call a method that would have changed this. Go idle.
            setDragState(STATE_IDLE)
        }
    }

    private fun clearMotionHistory() {
        if (mInitialMotionX == null) {
            return
        }
        Arrays.fill(mInitialMotionX, 0f)
        Arrays.fill(mInitialMotionY, 0f)
        Arrays.fill(mLastMotionX, 0f)
        Arrays.fill(mLastMotionY, 0f)
        Arrays.fill(mInitialEdgesTouched, 0)
        Arrays.fill(mEdgeDragsInProgress, 0)
        Arrays.fill(mEdgeDragsLocked, 0)
        mPointersDown = 0
    }

    private fun clearMotionHistory(pointerId: Int) {
        if (mInitialMotionX == null || !isPointerDown(pointerId)) {
            return
        }
        mInitialMotionX!![pointerId] = 0f
        mInitialMotionY!![pointerId] = 0f
        mLastMotionX!![pointerId] = 0f
        mLastMotionY!![pointerId] = 0f
        mInitialEdgesTouched!![pointerId] = 0
        mEdgeDragsInProgress!![pointerId] = 0
        mEdgeDragsLocked!![pointerId] = 0
        mPointersDown = mPointersDown and (1 shl pointerId).inv()
    }

    private fun ensureMotionHistorySizeForId(pointerId: Int) {
        if (mInitialMotionX == null || mInitialMotionX!!.size <= pointerId) {
            val imx = FloatArray(pointerId + 1)
            val imy = FloatArray(pointerId + 1)
            val lmx = FloatArray(pointerId + 1)
            val lmy = FloatArray(pointerId + 1)
            val iit = IntArray(pointerId + 1)
            val edip = IntArray(pointerId + 1)
            val edl = IntArray(pointerId + 1)

            if (mInitialMotionX != null) {
                System.arraycopy(mInitialMotionX, 0, imx, 0, mInitialMotionX!!.size)
                System.arraycopy(mInitialMotionY, 0, imy, 0, mInitialMotionY!!.size)
                System.arraycopy(mLastMotionX, 0, lmx, 0, mLastMotionX!!.size)
                System.arraycopy(mLastMotionY, 0, lmy, 0, mLastMotionY!!.size)
                System.arraycopy(mInitialEdgesTouched, 0, iit, 0, mInitialEdgesTouched!!.size)
                System.arraycopy(mEdgeDragsInProgress, 0, edip, 0, mEdgeDragsInProgress!!.size)
                System.arraycopy(mEdgeDragsLocked, 0, edl, 0, mEdgeDragsLocked!!.size)
            }

            mInitialMotionX = imx
            mInitialMotionY = imy
            mLastMotionX = lmx
            mLastMotionY = lmy
            mInitialEdgesTouched = iit
            mEdgeDragsInProgress = edip
            mEdgeDragsLocked = edl
        }
    }

    private fun saveInitialMotion(x: Float, y: Float, pointerId: Int) {
        ensureMotionHistorySizeForId(pointerId)
        mLastMotionX!![pointerId] = x
        mInitialMotionX!![pointerId] = mLastMotionX!![pointerId]
        mLastMotionY!![pointerId] = y
        mInitialMotionY!![pointerId] = mLastMotionY!![pointerId]
        mInitialEdgesTouched!![pointerId] = getEdgesTouched(x.toInt(), y.toInt())
        mPointersDown = mPointersDown or (1 shl pointerId)
    }

    private fun saveLastMotion(ev: MotionEvent) {
        val pointerCount = ev.pointerCount
        for (i in 0 until pointerCount) {
            val pointerId = ev.getPointerId(i)
            // If pointer is invalid then skip saving on ACTION_MOVE.
            if (!isValidPointerForActionMove(pointerId)) {
                continue
            }
            val x = ev.getX(i)
            val y = ev.getY(i)
            mLastMotionX!![pointerId] = x
            mLastMotionY!![pointerId] = y
        }
    }

    fun isPointerDown(pointerId: Int): Boolean {
        return (mPointersDown and (1 shl pointerId)) != 0
    }

    fun setDragState(state: Int) {
        mParentView.removeCallbacks(mSetIdleRunnable)
        if (viewDragState != state) {
            viewDragState = state
            mCallback.onViewDragStateChanged(state)
            if (viewDragState == STATE_IDLE) {
                capturedView = null
            }
        }
    }

    fun tryCaptureViewForDrag(toCapture: View?, pointerId: Int): Boolean {
        if (toCapture === capturedView && activePointerId == pointerId) {
            // Already done!
            return true
        }
        if (toCapture != null && mCallback.tryCaptureView(toCapture, pointerId)) {
            activePointerId = pointerId
            captureChildView(toCapture, pointerId)
            return true
        }
        return false
    }

    protected fun canScroll(v: View, checkV: Boolean, dx: Int, dy: Int, x: Int, y: Int): Boolean {
        if (v is ViewGroup) {
            val group = v
            val scrollX = v.getScrollX()
            val scrollY = v.getScrollY()
            val count = group.childCount
            // Count backwards - let topmost views consume scroll distance first.
            for (i in count - 1 downTo 0) {
                // TODO: Add versioned support here for transformed views.
                // This will not work for transformed views in Honeycomb+
                val child = group.getChildAt(i)
                if ((x + scrollX >= child.left) && (x + scrollX < child.right
                            ) && (y + scrollY >= child.top) && (y + scrollY < child.bottom
                            ) && canScroll(
                        child, true, dx, dy, x + scrollX - child.left,
                        y + scrollY - child.top
                    )
                ) {
                    return true
                }
            }
        }

        return checkV && (v.canScrollHorizontally(-dx) || v.canScrollVertically(-dy))
    }

    fun shouldInterceptTouchEvent(ev: MotionEvent): Boolean {
        val action = ev.actionMasked
        val actionIndex = ev.actionIndex

        if (action == MotionEvent.ACTION_DOWN) {
            // Reset things for a new event stream, just in case we didn't get
            // the whole previous stream.
            cancel()
        }

        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain()
        }
        mVelocityTracker!!.addMovement(ev)

        when (action) {
            MotionEvent.ACTION_DOWN -> {
                val x = ev.x
                val y = ev.y
                val pointerId = ev.getPointerId(0)
                saveInitialMotion(x, y, pointerId)

                val toCapture = findTopChildUnder(x.toInt(), y.toInt())

                // Catch a settling view if possible.
                if (toCapture === capturedView && viewDragState == STATE_SETTLING) {
                    tryCaptureViewForDrag(toCapture, pointerId)
                }

                val edgesTouched = mInitialEdgesTouched!![pointerId]
                if ((edgesTouched and mTrackingEdges) != 0) {
                    mCallback.onEdgeTouched(edgesTouched and mTrackingEdges, pointerId)
                }
            }

            MotionEvent.ACTION_POINTER_DOWN -> {
                val pointerId = ev.getPointerId(actionIndex)
                val x = ev.getX(actionIndex)
                val y = ev.getY(actionIndex)

                saveInitialMotion(x, y, pointerId)

                // A ViewDragHelper can only manipulate one view at a time.
                if (viewDragState == STATE_IDLE) {
                    val edgesTouched = mInitialEdgesTouched!![pointerId]
                    if ((edgesTouched and mTrackingEdges) != 0) {
                        mCallback.onEdgeTouched(edgesTouched and mTrackingEdges, pointerId)
                    }
                } else if (viewDragState == STATE_SETTLING) {
                    // Catch a settling view if possible.
                    val toCapture = findTopChildUnder(x.toInt(), y.toInt())
                    if (toCapture === capturedView) {
                        tryCaptureViewForDrag(toCapture, pointerId)
                    }
                }
            }

            MotionEvent.ACTION_MOVE -> {
                if (mInitialMotionX != null && mInitialMotionY != null) {


                    // First to cross a touch slop over a draggable view wins. Also report edge drags.
                    val pointerCount = ev.pointerCount
                    var i = 0
                    while (i < pointerCount) {
                        val pointerId = ev.getPointerId(i)

                        // If pointer is invalid then skip the ACTION_MOVE.
                        if (!isValidPointerForActionMove(pointerId)) {
                            i++
                            continue
                        }

                        val x = ev.getX(i)
                        val y = ev.getY(i)
                        val dx = x - mInitialMotionX!![pointerId]
                        val dy = y - mInitialMotionY!![pointerId]

                        val toCapture = findTopChildUnder(x.toInt(), y.toInt())
                        val pastSlop = toCapture != null && checkTouchSlop(toCapture, dx, dy)
                        if (pastSlop) {
                            // check the callback's
                            // getView[Horizontal|Vertical]DragRange methods to know
                            // if you can move at all along an axis, then see if it
                            // would clamp to the same value. If you can't move at
                            // all in every dimension with a nonzero range, bail.
                            val oldLeft = toCapture!!.left
                            val targetLeft = oldLeft + dx.toInt()
                            val newLeft: Int = mCallback.clampViewPositionHorizontal(
                                toCapture,
                                targetLeft, dx.toInt()
                            )
                            val oldTop = toCapture.top
                            val targetTop = oldTop + dy.toInt()
                            val newTop: Int = mCallback.clampViewPositionVertical(
                                toCapture, targetTop,
                                dy.toInt()
                            )
                            val hDragRange: Int = mCallback.getViewHorizontalDragRange(toCapture)
                            val vDragRange: Int = mCallback.getViewVerticalDragRange(toCapture)
                            if (((hDragRange == 0 || (hDragRange > 0 && newLeft == oldLeft))
                                        && (vDragRange == 0 || (vDragRange > 0 && newTop == oldTop)))
                            ) {
                                break
                            }
                        }
                        reportNewEdgeDrags(dx, dy, pointerId)
                        if (viewDragState == STATE_DRAGGING) {
                            // Callback might have started an edge drag
                            break
                        }

                        if (pastSlop && tryCaptureViewForDrag(toCapture, pointerId)) {
                            break
                        }
                        i++
                    }
                    saveLastMotion(ev)
                }
            }

            MotionEvent.ACTION_POINTER_UP -> {
                val pointerId = ev.getPointerId(actionIndex)
                clearMotionHistory(pointerId)
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                cancel()
            }
        }
        return viewDragState == STATE_DRAGGING
    }

    fun processTouchEvent(ev: MotionEvent) {
        val action = ev.actionMasked
        val actionIndex = ev.actionIndex

        if (action == MotionEvent.ACTION_DOWN) {
            // Reset things for a new event stream, just in case we didn't get
            // the whole previous stream.
            cancel()
        }

        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain()
        }
        mVelocityTracker!!.addMovement(ev)

        when (action) {
            MotionEvent.ACTION_DOWN -> {
                val x = ev.x
                val y = ev.y
                val pointerId = ev.getPointerId(0)
                val toCapture = findTopChildUnder(x.toInt(), y.toInt())

                saveInitialMotion(x, y, pointerId)

                // Since the parent is already directly processing this touch event,
                // there is no reason to delay for a slop before dragging.
                // Start immediately if possible.
                tryCaptureViewForDrag(toCapture, pointerId)

                val edgesTouched = mInitialEdgesTouched!![pointerId]
                if ((edgesTouched and mTrackingEdges) != 0) {
                    mCallback.onEdgeTouched(edgesTouched and mTrackingEdges, pointerId)
                }
            }

            MotionEvent.ACTION_POINTER_DOWN -> {
                val pointerId = ev.getPointerId(actionIndex)
                val x = ev.getX(actionIndex)
                val y = ev.getY(actionIndex)

                saveInitialMotion(x, y, pointerId)

                // A ViewDragHelper can only manipulate one view at a time.
                if (viewDragState == STATE_IDLE) {
                    // If we're idle we can do anything! Treat it like a normal down event.

                    val toCapture = findTopChildUnder(x.toInt(), y.toInt())
                    tryCaptureViewForDrag(toCapture, pointerId)

                    val edgesTouched = mInitialEdgesTouched!![pointerId]
                    if ((edgesTouched and mTrackingEdges) != 0) {
                        mCallback.onEdgeTouched(edgesTouched and mTrackingEdges, pointerId)
                    }
                } else if (isCapturedViewUnder(x.toInt(), y.toInt())) {
                    // We're still tracking a captured view. If the same view is under this
                    // point, we'll swap to controlling it with this pointer instead.
                    // (This will still work if we're "catching" a settling view.)

                    tryCaptureViewForDrag(capturedView, pointerId)
                }
            }

            MotionEvent.ACTION_MOVE -> {
                if (viewDragState == STATE_DRAGGING) {
                    // If pointer is invalid then skip the ACTION_MOVE.
                    if (isValidPointerForActionMove(activePointerId)) {
                        val index = ev.findPointerIndex(activePointerId)
                        val x = ev.getX(index)
                        val y = ev.getY(index)
                        val idx = (x - mLastMotionX!![activePointerId]).toInt()
                        val idy = (y - mLastMotionY!![activePointerId]).toInt()

                        dragTo(capturedView!!.left + idx, capturedView!!.top + idy, idx, idy)

                        saveLastMotion(ev)
                    }
                } else {
                    // Check to see if any pointer is now over a draggable view.
                    val pointerCount = ev.pointerCount
                    var i = 0
                    while (i < pointerCount) {
                        val pointerId = ev.getPointerId(i)

                        // If pointer is invalid then skip the ACTION_MOVE.
                        if (!isValidPointerForActionMove(pointerId)) {
                            i++
                            continue
                        }

                        val x = ev.getX(i)
                        val y = ev.getY(i)
                        val dx = x - mInitialMotionX!![pointerId]
                        val dy = y - mInitialMotionY!![pointerId]

                        reportNewEdgeDrags(dx, dy, pointerId)
                        if (viewDragState == STATE_DRAGGING) {
                            // Callback might have started an edge drag.
                            break
                        }

                        val toCapture = findTopChildUnder(x.toInt(), y.toInt())
                        if ((checkTouchSlop(toCapture, dx, dy)
                                    && tryCaptureViewForDrag(toCapture, pointerId))
                        ) {
                            break
                        }
                        i++
                    }
                    saveLastMotion(ev)
                }
            }

            MotionEvent.ACTION_POINTER_UP -> {
                val pointerId = ev.getPointerId(actionIndex)
                if (viewDragState == STATE_DRAGGING && pointerId == activePointerId) {
                    // Try to find another pointer that's still holding on to the captured view.
                    var newActivePointer = INVALID_POINTER
                    val pointerCount = ev.pointerCount
                    var i = 0
                    while (i < pointerCount) {
                        val id = ev.getPointerId(i)
                        if (id == activePointerId) {
                            // This one's going away, skip.
                            i++
                            continue
                        }

                        val x = ev.getX(i)
                        val y = ev.getY(i)
                        if ((findTopChildUnder(x.toInt(), y.toInt()) === capturedView
                                    && tryCaptureViewForDrag(capturedView, id))
                        ) {
                            newActivePointer = activePointerId
                            break
                        }
                        i++
                    }

                    if (newActivePointer == INVALID_POINTER) {
                        // We didn't find another pointer still touching the view, release it.
                        releaseViewForPointerUp()
                    }
                }
                clearMotionHistory(pointerId)
            }

            MotionEvent.ACTION_UP -> {
                if (viewDragState == STATE_DRAGGING) {
                    releaseViewForPointerUp()
                }
                cancel()
            }

            MotionEvent.ACTION_CANCEL -> {
                if (viewDragState == STATE_DRAGGING) {
                    dispatchViewReleased(0f, 0f)
                }
                cancel()
            }
        }
    }

    private fun reportNewEdgeDrags(dx: Float, dy: Float, pointerId: Int) {
        var dragsStarted = 0
        if (checkNewEdgeDrag(dx, dy, pointerId, EDGE_LEFT)) {
            dragsStarted = dragsStarted or EDGE_LEFT
        }
        if (checkNewEdgeDrag(dy, dx, pointerId, EDGE_TOP)) {
            dragsStarted = dragsStarted or EDGE_TOP
        }
        if (checkNewEdgeDrag(dx, dy, pointerId, EDGE_RIGHT)) {
            dragsStarted = dragsStarted or EDGE_RIGHT
        }
        if (checkNewEdgeDrag(dy, dx, pointerId, EDGE_BOTTOM)) {
            dragsStarted = dragsStarted or EDGE_BOTTOM
        }

        if (dragsStarted != 0) {
            mEdgeDragsInProgress!![pointerId] = mEdgeDragsInProgress!![pointerId] or dragsStarted
            mCallback.onEdgeDragStarted(dragsStarted, pointerId)
        }
    }

    private fun checkNewEdgeDrag(delta: Float, odelta: Float, pointerId: Int, edge: Int): Boolean {
        val absDelta = abs(delta.toDouble()).toFloat()
        val absODelta = abs(odelta.toDouble()).toFloat()

        if (((mInitialEdgesTouched!![pointerId] and edge) != edge) || ((mTrackingEdges and edge) == 0
                    ) || ((mEdgeDragsLocked!![pointerId] and edge) == edge
                    ) || ((mEdgeDragsInProgress!![pointerId] and edge) == edge
                    ) || (absDelta <= touchSlop && absODelta <= touchSlop)
        ) {
            return false
        }
        if (absDelta < absODelta * 0.5f && mCallback.onEdgeLock(edge)) {
            mEdgeDragsLocked!![pointerId] = mEdgeDragsLocked!![pointerId] or edge
            return false
        }
        return (mEdgeDragsInProgress!![pointerId] and edge) == 0 && absDelta > this.touchSlop
    }

    private fun checkTouchSlop(child: View?, dx: Float, dy: Float): Boolean {
        if (child == null) {
            return false
        }
        val checkHorizontal: Boolean = mCallback.getViewHorizontalDragRange(child) > 0
        val checkVertical: Boolean = mCallback.getViewVerticalDragRange(child) > 0

        if (checkHorizontal && checkVertical) {
            return dx * dx + dy * dy > touchSlop * touchSlop
        } else if (checkHorizontal) {
            return abs(dx.toDouble()) > touchSlop
        } else if (checkVertical) {
            return abs(dy.toDouble()) > touchSlop
        }
        return false
    }

    fun checkTouchSlop(directions: Int): Boolean {
        val count = mInitialMotionX!!.size
        for (i in 0 until count) {
            if (checkTouchSlop(directions, i)) {
                return true
            }
        }
        return false
    }

    fun checkTouchSlop(directions: Int, pointerId: Int): Boolean {
        if (!isPointerDown(pointerId)) {
            return false
        }

        val checkHorizontal = (directions and DIRECTION_HORIZONTAL) == DIRECTION_HORIZONTAL
        val checkVertical = (directions and DIRECTION_VERTICAL) == DIRECTION_VERTICAL

        val dx = mLastMotionX!![pointerId] - mInitialMotionX!![pointerId]
        val dy = mLastMotionY!![pointerId] - mInitialMotionY!![pointerId]

        if (checkHorizontal && checkVertical) {
            return dx * dx + dy * dy > touchSlop * touchSlop
        } else if (checkHorizontal) {
            return abs(dx.toDouble()) > touchSlop
        } else if (checkVertical) {
            return abs(dy.toDouble()) > touchSlop
        }
        return false
    }

    fun isEdgeTouched(edges: Int): Boolean {
        val count = mInitialEdgesTouched!!.size
        for (i in 0 until count) {
            if (isEdgeTouched(edges, i)) {
                return true
            }
        }
        return false
    }

    fun isEdgeTouched(edges: Int, pointerId: Int): Boolean {
        return isPointerDown(pointerId) && (mInitialEdgesTouched!![pointerId] and edges) != 0
    }

    private fun releaseViewForPointerUp() {
        mVelocityTracker!!.computeCurrentVelocity(1000, mMaxVelocity)
        val xvel = clampMag(
            mVelocityTracker!!.getXVelocity(activePointerId),
            minVelocity, mMaxVelocity
        )
        val yvel = clampMag(
            mVelocityTracker!!.getYVelocity(activePointerId),
            minVelocity, mMaxVelocity
        )
        dispatchViewReleased(xvel, yvel)
    }

    private fun dragTo(left: Int, top: Int, dx: Int, dy: Int) {
        var clampedX = left
        var clampedY = top
        val oldLeft = capturedView!!.left
        val oldTop = capturedView!!.top
        if (dx != 0) {
            clampedX = mCallback.clampViewPositionHorizontal(capturedView, left, dx)
            ViewCompat.offsetLeftAndRight((capturedView)!!, clampedX - oldLeft)
        }
        if (dy != 0) {
            clampedY = mCallback.clampViewPositionVertical(capturedView, top, dy)
            ViewCompat.offsetTopAndBottom((capturedView)!!, clampedY - oldTop)
        }

        if (dx != 0 || dy != 0) {
            val clampedDx = clampedX - oldLeft
            val clampedDy = clampedY - oldTop
            mCallback.onViewPositionChanged(
                capturedView, clampedX, clampedY,
                clampedDx, clampedDy
            )
        }
    }

    fun isCapturedViewUnder(x: Int, y: Int): Boolean {
        return isViewUnder(capturedView, x, y)
    }

    fun isViewUnder(view: View?, x: Int, y: Int): Boolean {
        if (view == null) {
            return false
        }
        return (x >= view.left
                ) && (x < view.right
                ) && (y >= view.top
                ) && (y < view.bottom)
    }

    fun findTopChildUnder(x: Int, y: Int): View? {
        val childCount = mParentView.childCount
        for (i in childCount - 1 downTo 0) {
            val child = mParentView.getChildAt(mCallback.getOrderedChildIndex(i))
            if ((x >= child.left) && (x < child.right
                        ) && (y >= child.top) && (y < child.bottom)
            ) {
                return child
            }
        }
        return null
    }

    private fun getEdgesTouched(x: Int, y: Int): Int {
        var result = 0

        if (x < mParentView.left + edgeSize) result = result or EDGE_LEFT
        if (y < mParentView.top + edgeSize) result = result or EDGE_TOP
        if (x > mParentView.right - edgeSize) result = result or EDGE_RIGHT
        if (y > mParentView.bottom - edgeSize) result = result or EDGE_BOTTOM

        return result
    }

    private fun isValidPointerForActionMove(pointerId: Int): Boolean {
        if (!isPointerDown(pointerId)) {
            Log.e(
                TAG, ("Ignoring pointerId=" + pointerId + " because ACTION_DOWN was not received "
                        + "for this pointer before ACTION_MOVE. It likely happened because "
                        + " ViewDragHelper did not receive all the events in the event stream.")
            )
            return false
        }
        return true
    }

    companion object {
        private val TAG = "ViewDragHelper"


        val INVALID_POINTER: Int = -1

        val STATE_IDLE: Int = 0


        val STATE_DRAGGING: Int = 1


        val STATE_SETTLING: Int = 2


        val EDGE_LEFT: Int = 1 shl 0


        val EDGE_RIGHT: Int = 1 shl 1

        val EDGE_TOP: Int = 1 shl 2


        val EDGE_BOTTOM: Int = 1 shl 3


        val EDGE_ALL: Int = EDGE_LEFT or EDGE_TOP or EDGE_RIGHT or EDGE_BOTTOM


        val DIRECTION_HORIZONTAL: Int = 1 shl 0


        val DIRECTION_VERTICAL: Int = 1 shl 1


        val DIRECTION_ALL: Int = DIRECTION_HORIZONTAL or DIRECTION_VERTICAL

        private val EDGE_SIZE = 20 // dp

        private val BASE_SETTLE_DURATION = 256 // ms
        private val MAX_SETTLE_DURATION = 600 // ms


        private val sInterpolator: Interpolator = LinearInterpolator()


        fun create(
            forParent: ViewGroup,
            cb: Callback
        ): MomentViewDragHelper {
            return MomentViewDragHelper(forParent.context, forParent, cb)
        }

        fun create(
            forParent: ViewGroup, sensitivity: Float,
            cb: Callback
        ): MomentViewDragHelper {
            val helper = create(forParent, cb)
            helper.touchSlop = (helper.touchSlop * (1 / sensitivity)).toInt()
            return helper
        }
    }
}