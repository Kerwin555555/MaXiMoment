package com.moment.app.utils

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Window
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import com.trello.lifecycle2.android.lifecycle.AndroidLifecycle
import com.trello.rxlifecycle3.LifecycleProvider
import com.trello.rxlifecycle3.LifecycleTransformer

abstract class BaseDialogFragment : DialogFragment() {
    private val provider
            : LifecycleProvider<Lifecycle.Event> = AndroidLifecycle.createLifecycleProvider(this)

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

    protected fun <T> bindToLifecycle(): LifecycleTransformer<T> {
        return provider.bindToLifecycle()
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
