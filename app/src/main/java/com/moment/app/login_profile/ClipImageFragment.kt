package com.moment.app.login_profile

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.moment.app.databinding.ClipImagePopUpWindowBinding
import com.moment.app.utils.BaseFragment
import com.moment.app.utils.popBackStackNowAllowingStateLoss

class ClipImageFragment : BaseFragment() {
    //private val viewModel by activityViewModels<ProfileViewModel>()
    private lateinit var binding: ClipImagePopUpWindowBinding

    var onConfirmListener: OnImageConfirmListener? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = ClipImagePopUpWindowBinding.inflate(inflater)
        return binding.root
    }

    // clipMode == isPreview
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (!requireArguments().getBoolean("clipMode", false)) {
            binding.layer.isVisible = false
            binding.clipImage.isPreview = true
        } else {
            binding.layer.isVisible = true
            binding.clipImage.isPreview = false
        }
        binding.root.isClickable = true
        binding.confirm.isSelected = true
        kotlin.runCatching {
            val file: String? = requireArguments().getSerializable("file") as String?
            if (file != null) {
                Glide.with(this).pauseAllRequests()
                Glide.with(this)
                    .setDefaultRequestOptions(
                        RequestOptions.noAnimation().diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                    ) // source 快！
                    .load(file)
                    .centerInside()   //自定义最简剪裁
                    .skipMemoryCache(true)
                    .into(binding.clipImage)
            }

            val uri: Uri? = requireArguments().getParcelable("uri") as Uri?
            if (uri != null) {
                Glide.with(this)
                    .setDefaultRequestOptions(
                        RequestOptions.noAnimation().diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                    ) // source 快！
                    .load(uri)
                    .centerInside()   //自定义最简剪裁
                    .skipMemoryCache(true)
                    .into(binding.clipImage)
            }


            binding.confirm.setOnClickListener {
                onConfirmListener?.onConfirm(binding.clipImage)
            }

            binding.cancel.setOnClickListener {
                context?.let {
                    (it as AppCompatActivity).supportFragmentManager.popBackStackNowAllowingStateLoss()
                }
            }
        }.onFailure {
            Log.d("zhouzheng", ""+it.message)
        }
    }
}

interface OnImageConfirmListener {
    fun onConfirm(clipImageView: ClipImageView)
}
