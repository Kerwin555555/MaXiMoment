package com.moment.app.main_profile_feed.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.utils.widget.ImageFilterView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.moment.app.R
import com.moment.app.databinding.DetailsToolbarBinding
import com.moment.app.databinding.ViewFeedPicturesHeaderBinding
import com.moment.app.image_viewer.loadFeedRemoteResource
import com.moment.app.main_profile.entities.PostBean
import com.moment.app.models.UserLoginManager
import com.moment.app.network.startCoroutine
import com.moment.app.network.toast
import com.moment.app.utils.BaseActivity
import com.moment.app.utils.ViewerPhoto
import com.moment.app.utils.applyPaddingsWithDefaultZero
import com.moment.app.utils.dp
import com.moment.app.utils.setImageResourceSelectedStateListDrawable
import com.moment.app.utils.showInImageViewer
import java.text.SimpleDateFormat
import java.util.Date

class ViewFeedPicturesHeader : ConstraintLayout, DetailsFeedView {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    private val binding = ViewFeedPicturesHeaderBinding.inflate(LayoutInflater.from(context), this)
    private var post: PostBean? = null

    private val adapter by lazy {
        Adapter()
    }

    init {
        applyPaddingsWithDefaultZero(bottom = 15.dp)
        binding.picturesPager.adapter = adapter
        binding.indicator.setViewPager(binding.picturesPager)
        binding.indicator.configureIndicator(
            6.dp, 6.dp, -1,
            R.anim.scale_indicator, 0, R.drawable.selected_circle,
            R.drawable.unselected_circle
        )

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
                this@ViewFeedPicturesHeader.post!!.liked = !!this@ViewFeedPicturesHeader.post!!.liked
                binding.like.isSelected =  this@ViewFeedPicturesHeader.post!!.liked
            }){
                it.toast()
            }
        }
    }

    override fun bindData(post: PostBean, toolbarBindng: DetailsToolbarBinding) {
        this.post = post
        bindCommon(post, toolbarBindng)

        //image logic
        adapter.setNewData(post.pics_shape)
        binding.indicator.setViewPager(binding.picturesPager)
        binding.indicator.configureIndicator(
            6.dp, 6.dp, -1,
            R.anim.scale_indicator, 0, R.drawable.selected_circle,
            R.drawable.unselected_circle
        )
    }

    private fun bindCommon(post: PostBean, toolbarBinding: DetailsToolbarBinding) {
        post.user_info?.let {
            Glide.with(this).load(it.avatar)
                .into(toolbarBinding.avatar)
            toolbarBinding.gender.bindGender(it)
            toolbarBinding.name.text = it.nickname
            UserLoginManager.getUserInfo()?.let { userInfo ->
                  if (userInfo.user_id == it.user_id) {
                      toolbarBinding.more.isVisible = false
                  } else {
                      toolbarBinding.more.isVisible = true
                  }
            }
        }
        binding.content.text = post.content
        toolbarBinding.back.setOnClickListener {
            (context as? AppCompatActivity?)?.finish()
        }
        binding.time.text = SimpleDateFormat("yyyy.MM.dd HH:mm")
            .format(Date(post.create_time!!.time.toLong()))

        binding.like.isSelected = post.liked

        binding.commentCount.text = "${post.comment_num}"
    }

    inner class Adapter: BaseQuickAdapter<ViewerPhoto.PicShape, BaseViewHolder>(null) {

        override fun onCreateDefViewHolder(parent: ViewGroup?, viewType: Int): BaseViewHolder {
            return  BaseViewHolder(ImageFilterView(mContext).apply {
                layoutParams = RecyclerView.LayoutParams(MATCH_PARENT, MATCH_PARENT)
                scaleType = ImageView.ScaleType.CENTER_CROP
                round = 4.dp.toFloat()
            })
        }
        //https://svgconverter.app/free  如何把大图换svg, image_sizer换成小图片
        override fun convert(helper: BaseViewHolder, item: ViewerPhoto.PicShape?) {
            (helper.itemView as ImageView).loadFeedRemoteResource(item?.fileKey)
            helper.itemView.setOnClickListener {
                kotlin.runCatching {
                    (helper.itemView as ImageFilterView).showInImageViewer(post!!.pics_shape!!, item!!)
                }
            }
        }
    }
}

interface DetailsFeedView {
    fun bindData(post: PostBean, binding: DetailsToolbarBinding)
}