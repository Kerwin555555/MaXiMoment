package com.moment.app.login_profile

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.activityViewModels
import com.blankj.utilcode.util.LogUtils
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.Target
import com.moment.app.databinding.ClipImagePopUpWindowBinding
import com.moment.app.models.LoginModel
import com.moment.app.network.startCoroutine
import com.moment.app.network.toast
import com.moment.app.utils.BaseFragment
import com.moment.app.utils.ProgressDialog
import com.moment.app.utils.popBackStackNowAllowingStateLoss
import com.moment.app.utils.toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException

class ClipImageFragment: BaseFragment() {
    private val viewModel by activityViewModels<ProfileViewModel>()


    private lateinit var binding: ClipImagePopUpWindowBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = ClipImagePopUpWindowBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.root.isClickable = true

        val uri: Uri? = requireArguments().getParcelable("uri")
        if (uri == null) {
            "Invalid picture selected".toast()
        }
        Glide.with(this).load(uri!!)
            .override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL).into(binding.clipImage)


        binding.confirm.isSelected = true
        binding.confirm.setOnClickListener {
            viewModel.saveAvatar(binding.clipImage)
        }

        binding.cancel.setOnClickListener {
            context?.let {
                (it as AppCompatActivity).supportFragmentManager.popBackStackNowAllowingStateLoss()
            }
        }
    }
}