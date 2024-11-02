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
import com.moment.app.utils.setOnAvoidMultipleClicksListener
import com.moment.app.utils.showAlbumInImageViewer
import com.moment.app.utils.showInImageViewer


class UploadImageAdapter(val viewModel: PostSubmissionViewModel): BaseQuickAdapter<Any, BaseViewHolder>(R.layout.uploading_images_item_view) {


    override fun convert(helper: BaseViewHolder, item: Any) {
        val image = helper.getView<ImageView>(R.id.image)
        if (item is Uri) {
            mContext?.let {
                Glide.with(it).load(item as Uri).into(image)
            }
            val photoPos = viewModel.getImages().entries.find { it.value == item }?.key ?: null
            helper.getView<FrameLayout>(R.id.touche_area).setOnAvoidMultipleClicksListener({
                if (photoPos == null) return@setOnAvoidMultipleClicksListener
                viewModel.dispatchAction(Action.RemoveNewPhotoAction.apply {
                    this.photoPos = photoPos //一个负数
                })
            }, 500)
            helper.itemView.setOnAvoidMultipleClicksListener({
                image.showInImageViewer(mutableListOf(item as Uri), item)

            }, 500)
        } else {
            mContext?.let {
                image.loadNoAnimResource((item as AlbumItemFile).path)
            }
            helper.getView<FrameLayout>(R.id.touche_area).setOnAvoidMultipleClicksListener({
                val map = viewModel.getImages()
                var position = -1
                for ((k, v) in map) {
                    if (v == item) {
                        position = k
                        break
                    }
                }
                if (position == -1) return@setOnAvoidMultipleClicksListener
                viewModel.dispatchAction(Action.RemoveImageAction.apply {
                    pos = position
                    file = item as AlbumItemFile
                })
            }, 500)
            helper.itemView.setOnAvoidMultipleClicksListener({
                image.showAlbumInImageViewer(mutableListOf((item as AlbumItemFile).path), item.path, item)
            }, 500)
        }
    }

}

