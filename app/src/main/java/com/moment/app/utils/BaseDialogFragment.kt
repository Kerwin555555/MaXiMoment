package com.moment.app.utils

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Window
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager

abstract class BaseDialogFragment : DialogFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        if (enableTransparentBackground()) {
            dialog.window!!.setBackgroundDrawable(ColorDrawable(windowColor()))
        }
        return dialog
    }

    protected fun enableTransparentBackground(): Boolean {
        return true
    }

    protected fun windowColor(): Int {
        return Color.TRANSPARENT
    }

    override fun dismiss() {
//        super.dismiss();
        dismissAllowingStateLoss()
    }

    override fun dismissAllowingStateLoss() {
        try {
            super.dismissAllowingStateLoss()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun show(manager: FragmentManager, tag: String?) {
        try {
            super.show(manager, tag)
        } catch (e: Exception) {
            //ignore
            e.printStackTrace()
        }
    }
}
