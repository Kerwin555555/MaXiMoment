package com.moment.app.main_profile_wall

import android.app.Application
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.utils.widget.ImageFilterView
import androidx.core.os.bundleOf
import androidx.core.view.setPadding
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.SizeUtils
import com.bumptech.glide.Glide
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.didi.drouter.annotation.Router
import com.gyf.immersionbar.ImmersionBar
import com.moment.app.MomentApp
import com.moment.app.R
import com.moment.app.databinding.ActivityEditWallPhotosBinding
import com.moment.app.hilt.app_level.MockData
import com.moment.app.images.Explorer
import com.moment.app.login_profile.ChooseAlbumFragment
import com.moment.app.main_profile_wall.dialogs.ReplaceDeleteDialog
import com.moment.app.utils.BaseActivity
import com.moment.app.utils.DialogUtils
import com.moment.app.utils.dp
import com.moment.app.utils.setOnSingleClickListener
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
@Router(scheme = ".*", host = ".*", path = "/edit/photos")
class EditPhotosWallActivity : BaseActivity() {

    @Inject
    lateinit var app: Application

    private val size by lazy {
        (app.resources.displayMetrics.widthPixels - 10.dp)/ 3
    }
    private lateinit var binding: ActivityEditWallPhotosBinding
    private lateinit var adapter: Adapter
    private var initialList: ArrayList<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditWallPhotosBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ImmersionBar.with(this).statusBarDarkFont(false).fitsSystemWindows(false).init()

        binding.cancel.setOnClickListener {
            finish()
        }
        binding.save.setOnClickListener {

        }
        adapter = Adapter()
        binding.rv.layoutManager = GridLayoutManager(this, 3)
        binding.rv.adapter = adapter

        initialList = ArrayList((intent.getSerializableExtra("fileIds") as ArrayList<String>).subList(0, 6))
        val wrapperList =
            mutableListOf(Wrapper(), Wrapper(), Wrapper(), Wrapper(), Wrapper(), Wrapper())
        for (index in 0 until initialList!!.size) {
            wrapperList[index].fileId = initialList!![index]
        }
        adapter.setNewData(wrapperList)
    }

    data class Wrapper(var uri: Uri? = null, var fileId: String? = null) {
        fun isEmpty(): Boolean{
            return uri == null && fileId == null
        }
    }

    inner class Adapter : BaseQuickAdapter<Wrapper, BaseViewHolder>(R.layout.item_choose_photos) {
        override fun onCreateDefViewHolder(parent: ViewGroup?, viewType: Int): BaseViewHolder {
            return super.onCreateDefViewHolder(parent, viewType).apply {
                itemView.layoutParams = RecyclerView.LayoutParams(size, size)
                itemView.setPadding(SizeUtils.dp2px(4.5f))
            }
        }

        override fun convert(helper: BaseViewHolder, item: Wrapper) {
            if (mContext == null) return
            val pos = helper.absoluteAdapterPosition
            val bg = helper.getView<ImageView>(R.id.bg)

            val filterView = helper.getView<ImageFilterView>(R.id.image)
            filterView.setImageResource(0)
            filterView.setOnSingleClickListener({
                 if (item.isEmpty()) {
                      this@EditPhotosWallActivity.supportFragmentManager
                         .beginTransaction()
                         .setCustomAnimations(
                             R.anim.f_slide_in_right, // 进入动画
                             0, // 退出动画（这里没有设置，所以为0）, // 退出动画（这里没有设置，所以为0）
                             0, // 弹出动画（这里没有设置，所以为0）
                             R.anim.f_slide_out_right ,  // 弹入动画（这里没有设置，所以为0）
                         )
                         .add(R.id.root_layout, ChooseAlbumFragment().apply {
                             arguments = bundleOf("extra_mode" to Explorer.MODE_ONLY_IMAGE)
                         }, "ChooseAlbumFragment").addToBackStack(null)
                         .commitAllowingStateLoss()
                 } else {
                     DialogUtils.show(this@EditPhotosWallActivity, ReplaceDeleteDialog().apply {
                         runnable = {

                         }
                     })
                 }
            }, 100)

            if (item.fileId != null) {
                Glide.with(mContext).asBitmap().load(getResourceIdFromFileId(item.fileId!!)).into(filterView)
            } else if (item.uri != null) {
                Glide.with(mContext).asBitmap().load(item.uri).into(filterView)
            }
        }
    }


    @MockData
    private fun getResourceIdFromFileId(id: String) : Int{
        //"0","1", "2", "3", "1", "2"
        return when (id) {
            "0" -> R.mipmap.pic1
            "1" -> R.mipmap.pic2
            "2" -> R.mipmap.pic4
            "3" -> R.mipmap.pic3
            else -> R.mipmap.light_profile
        }
    }
}