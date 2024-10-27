package com.moment.app.main_profile.views

import android.content.Context
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.constraintlayout.utils.widget.ImageFilterView
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
import com.moment.app.R
import com.moment.app.databinding.ViewMeHeaderBinding
import com.moment.app.datamodel.UserInfo
import com.moment.app.utils.dp
import com.moment.app.utils.isRTL
import com.moment.app.utils.setBgWithCornerRadiusAndColor

class ViewMeHeader : FrameLayout {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    private val binding = ViewMeHeaderBinding.inflate(LayoutInflater.from(context), this)
    private var userInfo: UserInfo? = null

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
    }

    fun bindData(userInfo: UserInfo) {

        adapter.setNewData(userInfo.imagesWallList)
        initCount(userInfo)


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
        binding.friendsCount.text = "${userInfo.friends_count}"
        binding.followingCount.text = "${userInfo.following_count}"
        binding.followerCount.text = "${userInfo.follower_count}"
    }
}

class HeaderAdapter : BaseQuickAdapter<String, BaseViewHolder>(null) {
    override fun onCreateDefViewHolder(parent: ViewGroup?, viewType: Int): BaseViewHolder {
        return BaseViewHolder(ImageFilterView(mContext).apply {
            scaleType = ImageView.ScaleType.CENTER_CROP
            setBackgroundColor(0xffEEEEEE.toInt())
            layoutParams = RecyclerView.LayoutParams(59.dp, 59.dp)
            round = 8.dp.toFloat()
        })
    }

    override fun convert(helper: BaseViewHolder, item: String?) {
        Glide.with(mContext)
            .setDefaultRequestOptions(
                RequestOptions.noAnimation().diskCacheStrategy(
                    DiskCacheStrategy.RESOURCE
                )
            )
            .load(
                when (item) {
                    "0" -> R.mipmap.pic1
                    "1" -> R.mipmap.pic2
                    "2" -> R.mipmap.pic3
                    else -> R.mipmap.pic4
                }
            )
            .dontTransform()
            .placeholder(R.mipmap.image_place_holder)
            .thumbnail(0.3f)
            .centerInside()
            .timeout(3000)
            .error(R.mipmap.image_place_holder)
            .addListener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>,
                    isFirstResource: Boolean
                ): Boolean {
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable,
                    model: Any,
                    target: Target<Drawable>?,
                    dataSource: DataSource,
                    isFirstResource: Boolean
                ): Boolean {
                    return false
                }

            })
            .into((helper.itemView as ImageFilterView))
    }
}