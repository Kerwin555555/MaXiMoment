package com.moment.app.main_feed_publish.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.moment.app.databinding.ItemViewCameraBinding
import com.moment.app.databinding.ItemViewImagesBinding
import com.moment.app.image_viewer.loadNoAnimResource
import com.moment.app.images.bean.MediaFile
import com.moment.app.main_feed_publish.PublishViewModel
import com.moment.app.main_feed_publish.dialogs.ChooseMediaDialog
import com.moment.app.main_feed_publish.extensions.Action
import com.moment.app.utils.DialogUtils
import com.moment.app.utils.checkCameraPermission
import com.moment.app.utils.checkReadWritePermission
import com.moment.app.utils.setOnAvoidMultipleClicksListener
import com.moment.app.utils.showAlbumInImageViewer
import com.moment.app.utils.toast


class CameraAlbumDataAdapter(var viewModel: PublishViewModel): BaseQuickAdapter<MediaFile, BaseViewHolder>(null) {
    companion object {
        val REQUEST_CODE_TAKE = 4576673
    }
    private val CAMERA = 0
    private val IMAGE = 1

    var goAndTakeAPhoto: (()->Unit)? = null
    var goAndChoosePhotos: (()->Unit)? = null

    var selectedMap: LinkedHashMap<Int, Any?> = viewModel.getImages()

    private val size by lazy {
        mContext.resources.displayMetrics.widthPixels / 4
    }

    override fun onCreateDefViewHolder(parent: ViewGroup?, viewType: Int): BaseViewHolder {
        when (viewType) {
            CAMERA -> {
                return PermissionItemViewHolder(
                    ItemViewCameraBinding.inflate(LayoutInflater.from(mContext), parent, false)
                        .apply {
                            root.layoutParams = RecyclerView.LayoutParams(size, size)
                        }
                )
            }

            IMAGE -> {
                return MediaTypeViewHolder(
                    ItemViewImagesBinding.inflate(
                        LayoutInflater.from(
                            mContext
                        ), parent, false
                    ).apply {
                        root.layoutParams = RecyclerView.LayoutParams(size, size)
                    })
            }

            else -> {
                return MediaTypeViewHolder(
                    ItemViewImagesBinding.inflate(
                        LayoutInflater.from(
                            mContext
                        ), parent, false
                    ).apply {
                        root.layoutParams = RecyclerView.LayoutParams(size, size)
                    })
            }
        }
    }

    override fun getDefItemViewType(position: Int): Int {
        return if (position == 0) CAMERA else IMAGE
    }

    override fun convert(helper: BaseViewHolder, item: MediaFile) {
        when (helper.itemViewType) {
            CAMERA -> {
                (helper as PermissionItemViewHolder).bindData()
            }

            IMAGE -> {
                (helper as MediaTypeViewHolder).bindData(item)
            }
        }
    }

    inner class PermissionItemViewHolder(val binding: ItemViewCameraBinding) :
        BaseViewHolder(binding.root) {
        fun bindData() {
             binding.root.setOnClickListener {
                 if (mContext == null) {
                     return@setOnClickListener
                 }
                 if (selectedMap.size == 9) {
                     "Maximum photos is reached".toast()
                     return@setOnClickListener
                 }
                 val hasPhotoPermission = mContext.checkReadWritePermission() || data.size > 1
                 val hasCameraPermission = mContext.checkCameraPermission()
                 if (hasCameraPermission) {
                     // 所有权限都有了 got to camera
                      goAndTakeAPhoto?.invoke()
                 } else if (hasPhotoPermission) {
                     // 没有相机权限
                     DialogUtils.show(mContext, ChooseMediaDialog().apply {
                         arguments = bundleOf("hasPhotoPermission" to true)
                         this.goAndTakeAPhoto = this@CameraAlbumDataAdapter.goAndTakeAPhoto
                     })
                 } else {
                     // 权限都都没有
                     DialogUtils.show(mContext, ChooseMediaDialog().apply {
                         arguments = bundleOf("hasPhotoPermission" to false)
                         this.goAndTakeAPhoto = this@CameraAlbumDataAdapter.goAndTakeAPhoto
                         this.goAndGetPhotos = this@CameraAlbumDataAdapter.goAndChoosePhotos
                     })
                 }
             }
        }
    }

    inner class MediaTypeViewHolder(val binding: ItemViewImagesBinding) :
        BaseViewHolder(binding.root) {
        fun bindData(item: MediaFile) {
            if (mContext != null) {
                binding.albumImage.loadNoAnimResource(item.path)
            }
            val position = absoluteAdapterPosition
            binding.selector.isSelected = selectedMap.containsKey(position)
            if (selectedMap.containsKey(position)) {
                binding.selector.text = "${selectedMap.keys.toMutableList().indexOf(position) + 1}"
            } else {
                binding.selector.text = ""
            }
            binding.toucheArea.setOnAvoidMultipleClicksListener({
                if (selectedMap.size >= 9 && !selectedMap.containsKey(position)) {
                    "Maximum photos is reached"
                    return@setOnAvoidMultipleClicksListener
                }
                if (selectedMap.containsKey(position)) {
                    viewModel.dispatchAction(Action.RemoveImageAction.apply {
                        this.pos = position
                        this.file = item
                    })
                } else {
                    viewModel.dispatchAction(Action.AddImageAction.apply {
                        this.pos = position
                        this.file = item
                    })
                }
            }, 500)
            binding.albumImage.setOnClickListener {
                binding.albumImage.showAlbumInImageViewer(mutableListOf(item.path), item.path)
            }
        }
    }
}