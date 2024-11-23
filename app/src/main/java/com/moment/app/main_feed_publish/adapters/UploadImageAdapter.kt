package com.moment.app.main_feed_publish.adapters

import android.net.Uri
import android.widget.FrameLayout
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.moment.app.R
import com.moment.app.image_viewer.loadNoAnimResource
import com.moment.app.localimages.datamodel.AlbumItemFile
import com.moment.app.main_feed_publish.PostSubmissionViewModel
import com.moment.app.main_feed_publish.extensions.Action
import com.moment.app.utils.clicks
import com.moment.app.utils.showAlbumInImageViewer
import com.moment.app.utils.showInImageViewer


class UploadImageAdapter(val viewModel: PostSubmissionViewModel): BaseQuickAdapter<Any, BaseViewHolder>(R.layout.uploading_images_item_view) {


    override fun convert(helper: BaseViewHolder, item: Any) {
        val image = helper.getView<ImageView>(R.id.image)
        if (item is Uri) {
            mContext?.let {
                Glide.with(it).load(item as Uri).into(image)
            }
            val photoAdapterPosition = helper.bindingAdapterPosition
            helper.getView<FrameLayout>(R.id.touche_area).clicks{
                if (photoAdapterPosition == null) return@clicks
                viewModel.dispatchAction(Action.RemoveNewPhotoAction.apply {
                    this.photoAdapterPosition = photoAdapterPosition
                })
            }
            helper.itemView.clicks{
                image.showInImageViewer(mutableListOf(item as Uri), item)
            }
        } else {
            mContext?.let {
                image.loadNoAnimResource((item as AlbumItemFile).path)
            }
            val imageUploadAdapterPostion = helper.bindingAdapterPosition
            helper.getView<FrameLayout>(R.id.touche_area).clicks{
                val map = viewModel.getImages()
                var imageAlbumePos = -1
                for ((k, v) in map) {
                    if (v == item) {
                        imageAlbumePos = k
                        break
                    }
                }
                if (imageAlbumePos == -1) return@clicks
                viewModel.dispatchAction(Action.RemoveImageAction.apply {
                    imageAlbumPosition = imageAlbumePos
                    imageUploadPosition = imageUploadAdapterPostion
                    file = item as AlbumItemFile
                })
            }
            helper.itemView.clicks {
                image.showAlbumInImageViewer(mutableListOf((item as AlbumItemFile).path), item.path, item)
            }
        }
    }

}

