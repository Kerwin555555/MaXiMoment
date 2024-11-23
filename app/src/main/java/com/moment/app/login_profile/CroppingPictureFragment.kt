package com.moment.app.login_profile

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.moment.app.databinding.ClipImagePopUpWindowBinding
import com.moment.app.utils.BaseFragment
import com.moment.app.utils.MOMENT_APP
import com.moment.app.utils.clicks
import com.moment.app.utils.copyFragmentArgumentsToMap
import com.moment.app.utils.getScreenHeight
import com.moment.app.utils.getScreenWidth
import com.moment.app.utils.popBackStackNowAllowingStateLoss
import java.lang.ref.WeakReference

class CroppingPictureFragment : BaseFragment() {
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
                val weakReference = WeakReference<PictureCroppingView>(binding.clipImage)
                Glide.with(this)
                    .asBitmap()
                    .load(file)
                    .centerInside()   //自定义最简剪裁
                    .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                    .skipMemoryCache(true)
                    .into(object : CustomTarget<Bitmap>(getScreenWidth(), getScreenHeight()) {
                        override fun onResourceReady(
                            resource: Bitmap,
                            transition: Transition<in Bitmap>?
                        ) {
                            val v = weakReference.get()
                            v?.let {
                                it.setImageDrawable(BitmapDrawable(it.context.resources,resource))
                            }
                        }

                        override fun onLoadCleared(placeholder: Drawable?) {

                        }
                    })
            }

            val uri: Uri? = requireArguments().getParcelable("uri") as Uri?
            //自己刚照的照片
            if (uri != null) {
                val weakReference = WeakReference<PictureCroppingView>(binding.clipImage)
                Glide.with(this)
                    .asBitmap()
                    .load(uri)
                    .centerInside()   //自定义最简剪裁
                    .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                    .skipMemoryCache(true)
                    .into(object : CustomTarget<Bitmap>(getScreenWidth(), getScreenHeight()) {
                        override fun onResourceReady(
                            resource: Bitmap,
                            transition: Transition<in Bitmap>?
                        ) {
                            val v = weakReference.get()
                            v?.let {
                                it.setImageDrawable(BitmapDrawable(it.context.resources,resource))
                            }
                        }

                        override fun onLoadCleared(placeholder: Drawable?) {

                        }
                    })
            }


            binding.confirm.clicks{
                val map = copyFragmentArgumentsToMap()
                if (file != null) {
                    map["file"] = file
                    onConfirmListener?.onConfirm(binding.clipImage, map)
                } else if (uri != null) {
                    map["uri"] = uri
                    Log.d(MOMENT_APP, "dxx xxxx" + (onConfirmListener == null))
                    onConfirmListener?.onConfirm(binding.clipImage, map)
                }
            }

            binding.cancel.setOnClickListener {
                context?.let {
                    (it as AppCompatActivity).supportFragmentManager.popBackStackNowAllowingStateLoss()
                }
            }
        }.onFailure {
            Log.d(MOMENT_APP, ""+it.message)
        }
    }
}

interface OnImageConfirmListener {
    /**
     *  clipImageView: 提供剪裁后的Bitmap, map: 其他启动信息 (目前 file 和 uri 是必须处理的信息)
     *  目前 profile页面只需剪裁的bitmap,
     */
    fun onConfirm(clipImageView: PictureCroppingView, map: Map<String, Any?>?)
}