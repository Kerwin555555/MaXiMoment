package com.moment.app.utils

import android.content.Context
import android.content.ContextWrapper
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import com.moment.app.databinding.ViewLoadingProgressBinding
import com.moment.app.ui.uiLibs.RefreshView

open class ProgressDialog : BaseDialogFragment() {
    private lateinit var binding: ViewLoadingProgressBinding

    var refreshView: RefreshView? = null
    private var onCancelListener: DialogInterface.OnCancelListener? = null

    override fun onStart() {
        super.onStart()
        dialog?.window?.setBackgroundDrawable(null)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = ViewLoadingProgressBinding.inflate(inflater)
        refreshView = RefreshView(requireContext()).apply {
            refreshView?.layoutParams = FrameLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT)
            binding.root.addView(this)
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (arguments != null) {
            val cancel: Boolean = arguments?.getBoolean("cancel", true) ?: true
            dialog?.setCanceledOnTouchOutside(cancel)
            dialog?.setCancelable(cancel)
        }
    }

    fun setOnCancelListener(onCancelListener: DialogInterface.OnCancelListener?) {
        this.onCancelListener = onCancelListener
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        if (onCancelListener != null) {
            onCancelListener!!.onCancel(dialog)
        }
    }

    override fun show(manager: FragmentManager, tag: String?) {
        super.show(manager, tag)
    }

    override fun dismiss() {
//        super.dismiss();
        try {
            if (refreshView != null) {

            }
            dismissAllowingStateLoss()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        //        refreshView.complete();
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }

    companion object {
        fun show(manager: FragmentManager): ProgressDialog {
            val dialog = ProgressDialog()
            dialog.show(manager, "pp")
            return dialog
        }

        fun show(context: Context?): ProgressDialog {
            val dialog = ProgressDialog()
            DialogUtils.show(context, dialog)
            return dialog
        }

        fun show(
            context: Context?,
            onCancelListener: DialogInterface.OnCancelListener?
        ): ProgressDialog {
            val dialog = ProgressDialog()
            dialog.setOnCancelListener(onCancelListener)
            DialogUtils.show(context, dialog)
            return dialog
        }

        private fun getActivity(context: Context): AppCompatActivity? {
            var context: Context? = context
            while (context is ContextWrapper) {
                if (context is AppCompatActivity) {
                    return context
                }
                context = context.baseContext
            }
            return null
        }

        fun showWithTitle(manager: FragmentManager, title: String?): ProgressDialog {
            val dialog = ProgressDialog()
            val bundle = Bundle()
            bundle.putString("title", title)
            dialog.setArguments(bundle)
            dialog.show(manager, "pp")
            return dialog
        }
    }
}