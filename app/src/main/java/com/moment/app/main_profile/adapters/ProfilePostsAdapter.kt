package com.moment.app.main_profile.adapters

import android.graphics.Color
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.moment.app.datamodel.UserInfo
import com.moment.app.main_profile.entities.PostBean
import com.moment.app.main_profile.views.AdapterItemView
import com.moment.app.main_profile.views.ImageItemView
import com.moment.app.main_profile.views.PureContentItemView

class ProfilePostsAdapter : BaseQuickAdapter<PostBean, BaseViewHolder>(null){
    companion object {
        const val VIEW_TYPE_PURE_TEXT = 1
        const val VIEW_TYPE_IMAGE = 2
    }

    //每一个Profile Posts Adapter 都属于一个user
    var userInfo: UserInfo? = null

    override fun onCreateDefViewHolder(parent: ViewGroup?, viewType: Int): BaseViewHolder {
        return when (viewType) {
            VIEW_TYPE_IMAGE -> {
                return BaseViewHolder(ImageItemView(mContext).apply {
                    layoutParams = RecyclerView.LayoutParams(-1, -2)
                    setBackgroundColor(Color.WHITE)
                })
            }
            VIEW_TYPE_PURE_TEXT -> {
                return BaseViewHolder(PureContentItemView(mContext).apply {
                    layoutParams = RecyclerView.LayoutParams(-1, -2)
                    setBackgroundColor(Color.WHITE)
                })
            }
            else -> BaseViewHolder(PureContentItemView(mContext))
        }
    }

    override fun convert(helper: BaseViewHolder, item: PostBean?) {
        item?.let { it.user_info = userInfo } //实时刷新userInfo
        (helper.itemView as AdapterItemView).bindData(item!!)
    }

    override fun getDefItemViewType(position: Int): Int {
        if (data[position].isPictureFeed()) {
            return VIEW_TYPE_IMAGE
        }
        return VIEW_TYPE_PURE_TEXT
    }
}