package com.moment.app.main_profile.views

import android.content.Context
import android.graphics.drawable.Drawable
import android.print.PrintAttributes.Margins
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.ImageView
import androidx.constraintlayout.utils.widget.ImageFilterView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.ScreenUtils
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.github.iielse.imageviewer.ImageViewerBuilder
import com.moment.app.R
import com.moment.app.databinding.ViewItemImageBinding
import com.moment.app.image_viewer.show
import com.moment.app.main_profile.entities.PostBean
import com.moment.app.network.startCoroutine
import com.moment.app.network.toast
import com.moment.app.utils.BaseActivity
import com.moment.app.utils.MOMENT_APP
import com.moment.app.utils.ViewerPhoto
import com.moment.app.utils.dp
import com.moment.app.utils.getScreenWidth
import com.moment.app.utils.gotoPostDetail
import com.moment.app.utils.requestNewSize
import com.moment.app.utils.setImageResourceSelectedStateListDrawable
import com.moment.app.utils.setOnAvoidMultipleClicksListener
import com.moment.app.utils.showInImageViewer
import java.text.SimpleDateFormat
import java.util.Date

class ImageItemView: ConstraintLayout , AdapterItemView{
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
    private val binding = ViewItemImageBinding.inflate(LayoutInflater.from(context), this)

    private var post: PostBean? = null

    private val adapter by lazy {
        Adapter()
    }

    init {
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
                this@ImageItemView.post!!.liked = !!this@ImageItemView.post!!.liked
                binding.like.isSelected =  this@ImageItemView.post!!.liked
            }){
                it.toast()
            }
        }
    }

    override fun bindData(post: PostBean) {
        this.post = post
        bindCommon(post)

        //image logic
        adapter.setNewData(post.pics_shape)
        binding.indicator.setViewPager(binding.picturesPager)
        binding.indicator.configureIndicator(
            6.dp, 6.dp, -1,
            R.anim.scale_indicator, 0, R.drawable.selected_circle,
            R.drawable.unselected_circle
        )
    }

    private fun bindCommon(post: PostBean) {
        Log.d(MOMENT_APP, ""+post.user_info!!.avatar)
        Glide.with(this).load(post.user_info!!.avatar)
            .into(binding.avatar)
        binding.gender.bindGender(post.user_info!!)
        binding.name.text = post.user_info!!.name
        binding.content.text = post.content

        binding.time.text = SimpleDateFormat("yyyy.MM.dd HH:mm")
            .format(Date(post.create_time!!.time.toLong()))

        binding.like.isSelected = post.liked

        binding.commentCount.text = "${post.comment_num}"

        binding.root.setOnAvoidMultipleClicksListener( {
            gotoPostDetail(post)
        },500)
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
            Glide.with(mContext)
                .setDefaultRequestOptions(RequestOptions.noAnimation().diskCacheStrategy(DiskCacheStrategy.RESOURCE))
                .load(R.mipmap.user_post_test_image)
                .dontTransform()
                .placeholder(R.drawable.moment)
                .thumbnail(0.3f)
                .centerInside()
                .override(ScreenUtils.getAppScreenWidth() * 3/5, ScreenUtils.getAppScreenHeight() * 3/5)
                .error(R.drawable.moment)
                .into(helper.itemView as ImageFilterView)

            helper.itemView.setOnClickListener {
                (helper.itemView as ImageFilterView).showInImageViewer(post!!.pics_shape!!, item!!)
            }
        }
    }
}

interface AdapterItemView {
    fun bindData(post: PostBean)
}