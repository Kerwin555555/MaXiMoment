package com.moment.app.main_profile_wall.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.moment.app.databinding.DialogPhotoChooseBottomSheetBinding
import com.moment.app.utils.BaseBottomSheetDialogFragment

class ReplaceDeleteDialog : BaseBottomSheetDialogFragment() {
    private lateinit var binding: DialogPhotoChooseBottomSheetBinding
    var runnable: (() -> Unit)? = null

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
        binding.takePhoto.text = "Replace"
        binding.chooseLibrary.text = "Delete"
        binding.takePhoto.setOnClickListener {

            dismissAllowingStateLoss()
        }
        binding.chooseLibrary.setOnClickListener {
            dismissAllowingStateLoss()
        }
    }
}