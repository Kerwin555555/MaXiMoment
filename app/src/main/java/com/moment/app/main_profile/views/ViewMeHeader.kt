package com.moment.app.main_profile.views

import android.content.Context
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.text.TextUtils
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.constraintlayout.utils.widget.ImageFilterView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.didi.drouter.api.DRouter
import com.moment.app.R
import com.moment.app.databinding.ViewMeHeaderBinding
import com.moment.app.datamodel.UserInfo
import com.moment.app.image_viewer.loadNoAnimResource
import com.moment.app.utils.Constants
import com.moment.app.utils.ViewerPhoto
import com.moment.app.utils.dp
import com.moment.app.utils.formatScore
import com.moment.app.utils.isRTL
import com.moment.app.utils.setBgWithCornerRadiusAndColor
import com.moment.app.utils.setOnAvoidMultipleClicksListener
import com.moment.app.utils.showInImageViewer

class ViewMeHeader : ConstraintLayout {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    private val binding = ViewMeHeaderBinding.inflate(LayoutInflater.from(context), this)
    private var userInfo: UserInfo? = null
    var isMe = false
    var isOpen = false

    private val adapter by lazy {
        HeaderAdapter()
    }

    init {
        binding.location.setBgWithCornerRadiusAndColor(9f, solid = 0x1aed9d1d)

        binding.photoRv.layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
        binding.photoRv.adapter = adapter
        binding.photoRv.addItemDecoration(object : ItemDecoration(){
            override fun getItemOffsets(
                outRect: Rect,
                view: View,
                parent: RecyclerView,
                state: RecyclerView.State
            ) {
                super.getItemOffsets(outRect, view, parent, state)
                val pos = parent.getChildAdapterPosition(view)
                if (pos != adapter.data.size - 1) {
                    if (!parent.isRTL()) outRect.right = 8.dp
                    else outRect.left = 8.dp
                }
            }
        })

        binding.photoClick.setOnClickListener {
            kotlin.runCatching {
                binding.photoClick.showInImageViewer(mutableListOf(userInfo!!.avatar!!), userInfo!!.avatar!!)
            }
        }
    }

    fun init(isMe: Boolean) {
        this.isMe = isMe
        if (isMe) {
            binding.editPhotos.isVisible = true
        } else {
            binding.editPhotos.visibility = View.INVISIBLE
        }
        if (isMe) {
            binding.edit.isVisible = true
            binding.expand.isVisible = false
        } else {
            binding.edit.isVisible = false
            binding.expand.isVisible = true
        }
    }

    fun bindData(userInfo: UserInfo, isMe: Boolean) {
        this.userInfo = userInfo
        this.isMe = isMe
        if (isMe) {
            binding.editPhotos.isVisible = true
            binding.editPhotos.setOnAvoidMultipleClicksListener({
                DRouter.build("/edit/photos")
                    .putExtra("fileIds", ArrayList(userInfo.imagesWallList)).start()
            }, 500)
        } else {
            binding.editPhotos.visibility = View.INVISIBLE
        }
        adapter.setNewData(userInfo.imagesWallList)
        initCount(userInfo)

        if (isMe) {
            binding.edit.isVisible = true
            binding.expand.isVisible = false
            binding.edit.setOnAvoidMultipleClicksListener({
                DRouter.build("/edit/userInfo").start()
            }, 500)
        } else {
            binding.edit.isVisible = false
            binding.expand.isVisible = true
            binding.bio.maxLines = 2
            binding.bio.ellipsize = TextUtils.TruncateAt.END
            binding.expand.setOnAvoidMultipleClicksListener({
                if (isOpen) {
                    binding.bio.maxLines = 2
                    isOpen = false
                    binding.ivArrow.rotation = if (binding.expand.isRTL()) 270f else 90f
                } else {
                    binding.bio.maxLines = 1000_000
                    binding.ivArrow.rotation = 0f
                    isOpen = true
                }
            }, 500)
        }


        binding.gender.bindGender(userInfo)
        //location

        if (!userInfo.bio.isNullOrEmpty()) {
            binding.bio.isVisible = true
            binding.bio.text = userInfo.bio
        } else {
            binding.bio.isVisible = false
        }
    }

    private fun initCount(userInfo: UserInfo) {
        if (isMe) {
            binding.friends.isVisible = true
            binding.friendsCount.text = "${formatScore(userInfo.friends_count!!.toLong())}"
        } else {
            binding.friends.isVisible = false
        }
        binding.followingCount.text = "${formatScore(userInfo.following_count!!.toLong())}"
        binding.followerCount.text = "${formatScore(userInfo.follower_count!!.toLong())}"
    }


    inner class HeaderAdapter : BaseQuickAdapter<String, BaseViewHolder>(null) {
        override fun onCreateDefViewHolder(parent: ViewGroup?, viewType: Int): BaseViewHolder {
            return BaseViewHolder(ImageFilterView(mContext).apply {
                scaleType = ImageView.ScaleType.CENTER_CROP
                setBackgroundColor(0xffEEEEEE.toInt())
                layoutParams = RecyclerView.LayoutParams(59.dp, 59.dp)
                round = 8.dp.toFloat()
            })
        }

        override fun convert(helper: BaseViewHolder, item: String?) {
            (helper.itemView as ImageView).loadNoAnimResource(item ?: "")
            helper.itemView.setOnClickListener {
                kotlin.runCatching {
                    (helper.itemView as ImageFilterView).showInImageViewer(
                        userInfo!!.imagesWallList,
                        item ?: ""
                    )
                }
            }
        }
    }
}