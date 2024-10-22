package com.moment.app.utils

import android.os.Bundle
import android.view.View
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.moment.app.R
import org.greenrobot.eventbus.EventBus

open class BaseBottomSheetDialogFragment : BottomSheetDialogFragment() {
    private var bottomSheetBehavior: BottomSheetBehavior<*>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (this.dialog != null && this.enableAutoExpanded()) {
            this.dialog!!.setOnShowListener {
                val bottomSheetInternal =
                    dragView
                if (bottomSheetInternal != null) {
                    try {
                        this@BaseBottomSheetDialogFragment.bottomSheetBehavior =
                            BottomSheetBehavior.from(bottomSheetInternal)
                        bottomSheetBehavior?.setState(
                            BottomSheetBehavior.STATE_EXPANDED
                        )
                        bottomSheetBehavior?.setSkipCollapsed(
                            true
                        )
                        bottomSheetBehavior?.setDraggable(
                            enableDraggable()
                        )
                    } catch (var4: Exception) {
                        val e = var4
                        e.printStackTrace()
                    }
                }
            }
        }

        try {
            EventBus.getDefault().register(this)
        } catch (var4: Exception) {
        }
    }

    fun setDraggable(draggable: Boolean) {
        if (this.bottomSheetBehavior != null) {
            bottomSheetBehavior!!.isDraggable = draggable
        }
    }

    protected val dragView: View?
        get() = if (this.dialog != null) this.dialog!!.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet) else null

    protected fun enableDraggable(): Boolean {
        return true
    }

    protected fun enableAutoExpanded(): Boolean {
        return true
    }

    override fun onDestroyView() {
        try {
            EventBus.getDefault().unregister(this)
        } catch (var2: Exception) {
        }

        super.onDestroyView()
    }
}
