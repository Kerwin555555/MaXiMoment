package com.moment.app.utils

import android.content.Context
import android.content.ContextWrapper
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import com.moment.app.databinding.ViewLoadingProgressBinding

open class ProgressIndicatorFragment : BaseDialogFragment() {
    private lateinit var binding: ViewLoadingProgressBinding

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
        fun show(manager: FragmentManager): ProgressIndicatorFragment {
            val dialog = ProgressIndicatorFragment()
            dialog.show(manager, "pp")
            return dialog
        }

        fun show(context: Context?): ProgressIndicatorFragment {
            val dialog = ProgressIndicatorFragment()
            DialogFragmentManager.show(context, dialog)
            return dialog
        }

        fun show(
            context: Context?,
            onCancelListener: DialogInterface.OnCancelListener?
        ): ProgressIndicatorFragment {
            val dialog = ProgressIndicatorFragment()
            dialog.setOnCancelListener(onCancelListener)
            DialogFragmentManager.show(context, dialog)
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

        fun showWithTitle(manager: FragmentManager, title: String?): ProgressIndicatorFragment {
            val dialog = ProgressIndicatorFragment()
            val bundle = Bundle()
            bundle.putString("title", title)
            dialog.setArguments(bundle)
            dialog.show(manager, "pp")
            return dialog
        }
    }
}