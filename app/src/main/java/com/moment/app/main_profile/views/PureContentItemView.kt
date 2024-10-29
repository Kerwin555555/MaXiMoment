package com.moment.app.main_profile.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.bumptech.glide.Glide
import com.moment.app.R
import com.moment.app.databinding.ViewItemTextBinding
import com.moment.app.main_profile.entities.PostBean
import com.moment.app.network.startCoroutine
import com.moment.app.network.toast
import com.moment.app.utils.BaseActivity
import com.moment.app.utils.gotoPostDetail
import com.moment.app.utils.setImageResourceSelectedStateListDrawable
import com.moment.app.utils.setOnSingleClickListener
import java.text.SimpleDateFormat
import java.util.Date

class PureContentItemView: FrameLayout, AdapterItemView {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )
    private var post: PostBean? = null
    private val binding = ViewItemTextBinding.inflate(LayoutInflater.from(context), this)

    init {
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
                    this@PureContentItemView.post!!.liked = !this@PureContentItemView.post!!.liked
                    binding.like.isSelected =  this@PureContentItemView.post!!.liked
                }){
                    it.toast()
                }
            }
    }

    override fun bindData(post: PostBean) {
        this.post = post
        bindCommon(post)

    }

    private fun bindCommon(post: PostBean) {
        Glide.with(this).load(post.user_info!!.avatar)
            .into(binding.avatar)
        binding.name.text = post.user_info!!.name
        binding.content.text = post.content
        binding.gender.bindGender(post.user_info!!)

        binding.time.text = SimpleDateFormat("yyyy.MM.dd HH:mm")
            .format(Date(post.create_time!!.time.toLong()))
        binding.root.setOnSingleClickListener( {
            gotoPostDetail(post)
        },500)
        binding.like.isSelected = post.liked

        binding.commentCount.text = "${post.comment_num}"

    }
}