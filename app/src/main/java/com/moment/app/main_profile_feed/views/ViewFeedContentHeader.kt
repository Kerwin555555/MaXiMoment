package com.moment.app.main_profile_feed.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.bumptech.glide.Glide
import com.moment.app.R
import com.moment.app.databinding.DetailsToolbarBinding
import com.moment.app.databinding.ViewFeedHeaderBinding
import com.moment.app.main_profile.entities.PostBean
import com.moment.app.network.startCoroutine
import com.moment.app.network.toast
import com.moment.app.utils.BaseActivity
import com.moment.app.utils.applyPaddingsWithDefaultZero
import com.moment.app.utils.dp
import com.moment.app.utils.setImageResourceSelectedStateListDrawable
import java.text.SimpleDateFormat
import java.util.Date

class ViewFeedContentHeader : ConstraintLayout, DetailsFeedView{

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    private var post: PostBean? = null
    private val binding = ViewFeedHeaderBinding.inflate(LayoutInflater.from(context), this)

    init {
        applyPaddingsWithDefaultZero(bottom = 15.dp)
        binding.like.setImageResourceSelectedStateListDrawable(
            selectedId = R.mipmap.home_selected,
            unSelectedId = R.mipmap.thumb_up
        )

        binding.like.setOnClickListener {
            if (this.post == null) {
                return@setOnClickListener
            }
            //thumbupolock
            (context as? BaseActivity?)?.startCoroutine({
                //calllike service
                this@ViewFeedContentHeader.post!!.liked = !this@ViewFeedContentHeader.post!!.liked
                binding.like.isSelected =  this@ViewFeedContentHeader.post!!.liked
            }){
                it.toast()
            }
        }
    }

    override fun bindData(post: PostBean,detailBinding :DetailsToolbarBinding) {
        this.post = post
        bindCommon(post, detailBinding)
    }

    private fun bindCommon(post: PostBean, detailBinding:DetailsToolbarBinding) {
        post.user_info?.let {
            Glide.with(this).load(it.avatar)
                .into(detailBinding.avatar)
            detailBinding.gender.bindGender(it)
            detailBinding.name.text = it.name
        }
        binding.content.text = post.content
        detailBinding.back.setOnClickListener {
            (context as? AppCompatActivity?)?.finish()
        }
        binding.time.text = SimpleDateFormat("yyyy.MM.dd HH:mm")
            .format(Date(post.create_time!!.time.toLong()))

        binding.like.isSelected = post.liked

        binding.commentCount.text = "${post.comment_num}"

    }
}