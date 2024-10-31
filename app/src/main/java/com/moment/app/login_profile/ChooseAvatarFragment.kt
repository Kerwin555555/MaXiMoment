package com.moment.app.login_profile

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import com.bumptech.glide.Glide
import com.moment.app.R
import com.moment.app.databinding.AvatarPopWindowBinding
import com.moment.app.images.Explorer
import com.moment.app.models.LoginModel
import com.moment.app.permissions.PermissionHelper
import com.moment.app.utils.BaseFragment
import com.moment.app.utils.MOMENT_APP
import com.moment.app.utils.bottomInBottomOut
import com.moment.app.utils.toast


class ChooseAvatarFragment : BaseFragment() {
    private lateinit var binding: AvatarPopWindowBinding
    private val viewModel by activityViewModels<ProfileViewModel>()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = AvatarPopWindowBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.isClickable = true
        binding.pic.setOnClickListener {
            onChooseFromLibrary()
        }

        viewModel.hasAvatarLiveData.observe(this.viewLifecycleOwner) {
            if (it == true) {
                Glide.with(this).load(LoginModel.getUserInfo()!!.avatar).dontTransform().into(binding.pic)
                binding.upload.isVisible = false
            } else {
                binding.upload.isVisible = true
                Glide.with(this).clear(binding.pic)
            }
        }
    }

    fun onChooseFromLibrary() {
        try {
            PermissionHelper.check(
                requireContext(), "Choose from library",
                arrayOf<String>(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ), object : PermissionHelper.Callback {
                    override fun result(res: Int) {
                        if (res == 0) {
                            Log.d(MOMENT_APP, Thread.currentThread().name)
                            choosePhoto()
                        }
                    }
                })
        } catch (e: Exception) {
            e.printStackTrace()
            (e.message + "requestPermissions error").toast()
        }
    }

    private fun choosePhoto() {
         (activity as? AppCompatActivity?)?.bottomInBottomOut()
           ?.add(R.id.root_layout, ChooseAlbumFragment().apply {
                   arguments = bundleOf("extra_mode" to Explorer.MODE_ONLY_IMAGE)
           }, "ChooseAlbumFragment")?.addToBackStack(null)
            ?.commitAllowingStateLoss()
    }
}