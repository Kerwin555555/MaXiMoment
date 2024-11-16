package com.moment.app.main_profile_feed.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.moment.app.databinding.FirstLayerCommentBinding
import com.moment.app.datamodel.CommentItem
import java.text.SimpleDateFormat
import java.util.Date

class PostDetailCommentsAdapter : BaseQuickAdapter<CommentItem, PostDetailCommentsAdapter.CommentItemHolder>(null){

    override fun onCreateDefViewHolder(parent: ViewGroup?, viewType: Int): PostDetailCommentsAdapter.CommentItemHolder {
        val binding = FirstLayerCommentBinding.inflate(LayoutInflater.from(mContext), parent!!, false).apply {
            root.layoutParams = RecyclerView.LayoutParams(-1, -2)
        }
        return CommentItemHolder(binding)
    }

    override fun convert(holder: PostDetailCommentsAdapter.CommentItemHolder, item: CommentItem) {
        val binding = holder.binding
        item.user_info?.let {
            binding.name.text = item.user_info?.nickname ?: ""
            Glide.with(mContext).load(it.avatar).into(binding.avatar)
            binding.gender.bindGender(it)
        }
        binding.like.isSelected = item.comment_liked
        binding.likeCount.text = "${item.comment_like_num}"
        binding.time.text = SimpleDateFormat("yyyy.MM.dd HH:mm")
            .format(Date(item.time_info!!.time.toLong()))

        binding.commentAdd.setOnClickListener {
            //Add comment and keyboard up
        }

    }

    override fun getDefItemViewType(position: Int): Int {
        return super.getDefItemViewType(position)
    }

    data class CommentItemHolder(val binding: FirstLayerCommentBinding): BaseViewHolder(binding.root)
}