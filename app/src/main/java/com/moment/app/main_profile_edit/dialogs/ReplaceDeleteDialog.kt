package com.moment.app.main_profile_edit.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.moment.app.databinding.DialogPhotoChooseBottomSheetBinding
import com.moment.app.utils.BaseBottomSheetDialogFragment

class ReplaceDeleteDialog : BaseBottomSheetDialogFragment() {
    private lateinit var binding: DialogPhotoChooseBottomSheetBinding
    var onReplaceListener: OnReplaceListener? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DialogPhotoChooseBottomSheetBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (requireArguments().getBoolean("hideDelete")) {
            binding.chooseLibrary.isVisible = false
        }
        binding.takePhoto.text = "Replace"
        binding.chooseLibrary.text = "Delete"
        binding.takePhoto.setOnClickListener {
            onReplaceListener?.onReplace()
            dismissAllowingStateLoss()
        }
        binding.chooseLibrary.setOnClickListener {
            onReplaceListener?.onDelete()
            dismissAllowingStateLoss()
        }
    }

    interface OnReplaceListener {
        fun onReplace()
        fun onDelete()
    }
}