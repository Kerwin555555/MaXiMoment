package com.moment.app.main_feed_publish.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.moment.app.R
import com.moment.app.databinding.DialogPhotoChooseBottomSheetBinding


class ChooseAlbumDialog : BottomSheetDialogFragment() {
    private lateinit var binding: DialogPhotoChooseBottomSheetBinding

    var goAndTakeAPhoto: (()->Unit)? = null
    var goAndGetPhotos: (()->Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme)
    }


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
        binding.chooseLibrary.isVisible = !requireArguments().getBoolean("hasPhotoPermission")
        binding.takePhoto.setOnClickListener {
            goAndTakeAPhoto?.invoke()
            dismissAllowingStateLoss()
        }

        binding.chooseLibrary.setOnClickListener {
            goAndGetPhotos?.invoke()
            dismissAllowingStateLoss()
        }
    }
}