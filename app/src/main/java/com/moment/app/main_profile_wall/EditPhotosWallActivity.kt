package com.moment.app.main_profile_wall

import android.app.Application
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.widget.ImageView
import androidx.constraintlayout.utils.widget.ImageFilterView
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.view.setPadding
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.blankj.utilcode.util.BarUtils
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.SizeUtils
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.didi.drouter.annotation.Router
import com.gyf.immersionbar.ImmersionBar
import com.moment.app.R
import com.moment.app.databinding.ActivityEditWallPhotosBinding
import com.moment.app.eventbus.UpdateUserInfoEvent
import com.moment.app.hilt.app_level.MockData
import com.moment.app.images.Explorer
import com.moment.app.login_profile.ChooseAlbumFragment
import com.moment.app.login_profile.ClipImageView
import com.moment.app.login_profile.OnImageConfirmListener
import com.moment.app.main_profile_wall.dialogs.ReplaceDeleteDialog
import com.moment.app.network.startCoroutine
import com.moment.app.network.toast
import com.moment.app.permissions.MomentActionDialog
import com.moment.app.permissions.SetupBundle
import com.moment.app.utils.BaseActivity
import com.moment.app.utils.BaseBean
import com.moment.app.utils.DialogUtils
import com.moment.app.utils.ProgressDialog
import com.moment.app.utils.applyMargin
import com.moment.app.utils.bottomInBottomOut
import com.moment.app.utils.cleanSaveFragments
import com.moment.app.utils.dp
import com.moment.app.utils.saveView
import com.moment.app.utils.setOnSingleClickListener
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import org.greenrobot.eventbus.EventBus
import java.util.Collections
import javax.inject.Inject


@AndroidEntryPoint
@Router(scheme = ".*", host = ".*", path = "/edit/photos")
class EditPhotosWallActivity : BaseActivity(), OnImageConfirmListener{

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
        cleanSaveFragments()
        ImmersionBar.with(this).statusBarDarkFont(false).fitsSystemWindows(false).init()
        setSwipeBackEnable(false)
        binding.cancel.applyMargin(top = 15.dp + BarUtils.getStatusBarHeight())
        binding.cancel.setOnClickListener {
            onBackPressed()
        }
        adapter = Adapter()
        binding.rv.layoutManager = GridLayoutManager(this, 3)
        binding.rv.adapter = adapter

        initialList = (intent.getSerializableExtra("fileIds") as ArrayList<String>)
        val wrapperList =
            mutableListOf(Wrapper(), Wrapper(), Wrapper(), Wrapper(), Wrapper(), Wrapper())
        for (index in 0 until initialList!!.size) {
            wrapperList[index].remoteFileId = initialList!![index]
        }
        adapter.setNewData(wrapperList)
        binding.save.setOnClickListener {
            val progresDialog = ProgressDialog.show(this@EditPhotosWallActivity)
            progresDialog.isCancelable = false
            startCoroutine({
                val list = mutableListOf<Deferred<String?>>()
                for (item in adapter.data) {
                    if (item.remoteFileId != null) {
                        list.add(this.async(Dispatchers.IO) {
                            item.remoteFileId
                        })
                    } else if (item.albumOriginal != null){
                        list.add(this.async(Dispatchers.IO) {
                            val drawable = Glide.with(this@EditPhotosWallActivity).load(item.albumOriginal).
                            centerInside()
                                .submit(ScreenUtils.getAppScreenWidth()*3/5, ScreenUtils.getAppScreenHeight()*3/5).get()
                            saveView(this@EditPhotosWallActivity, (drawable as BitmapDrawable).bitmap)?.absolutePath ?: ""
                        })
                    }
                }
                val result = list.awaitAll()
                delay(400) //mock upload to cloud and backend
                progresDialog.dismissAllowingStateLoss()
                EventBus.getDefault().post(UpdateUserInfoEvent())
                finish()
            }) {
                it.toast()
                progresDialog.dismissAllowingStateLoss()
            }
        }
        ItemTouchHelper(CustomTouchCallback(
            binding.rv, adapter) {

        }).attachToRecyclerView(binding.rv)

        (binding.rv.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false //remove shimmer
    }

    override fun onBackPressed() {
        for (i in 0 until initialList!!.size) {
            if (adapter.data[i].remoteFileId != initialList?.get(i)) {
                showSaveReminderDialog()
                //shodialog
                return
            }
        }
        for (i in initialList!!.size until 6) {
            if (!adapter.data[i].isEmpty()) {
                showSaveReminderDialog()
                return
            }
        }
        super.onBackPressed()
    }

    fun showSaveReminderDialog() {
        MomentActionDialog.showBundle(this@EditPhotosWallActivity, object : SetupBundle {
            override fun setBundle(bundle: Bundle): Bundle {
                bundle.putString("content", "Do you want leave as your changes haven't been saved!")
                return bundle
            }
            override fun cancel() {}
            override fun confirm() {
                super@EditPhotosWallActivity.onBackPressed()
            }
        })
    }

    @MockData
    private fun getResourceIdFromFileId(id: String) : Int{
        //"0","1", "2", "3", "1", "2"
        return when (id) {
            "0" -> R.mipmap.pic2
            "1" -> R.mipmap.pic1
            "2" -> R.mipmap.pic4
            "3" -> R.mipmap.pic3
            else -> R.mipmap.light_profile
        }
    }

    override fun onConfirm(clipImageView: ClipImageView, map: Map<String, Any?>?) {
        kotlin.runCatching {
            supportFragmentManager.let {
                val f = it.findFragmentByTag("ClipImageFragment")
                val f2 = it.findFragmentByTag("ChooseAlbumFragment")
                it.beginTransaction()
                    .setCustomAnimations(0,R.anim.slide_down,0,0)
                    .remove(f!!)
                    .remove(f2!!)
                    .commitNowAllowingStateLoss()
            }
        }
        if (map?.containsKey("item") == true) {
            val item = map["item"] as? Wrapper?
            item?.let {
                item.albumOriginal = map["file"] as? String?
                item.remoteFileId = null
                reArrangeData()
                adapter.notifyItemRangeChanged(0, 6)
            } ?: let {

            }
        }
        //Glide.with(this).load("xxx").fitCenter().submit(ScreenUtils.getAppScreenWidth(), ScreenUtils.getAppScreenHeight()).get()

    }

    private fun reArrangeData() {
        val data = adapter.data
        var idx = 0
        for (index in 0 until data.size) {
            if (!data[index].isEmpty()) {
                val temp = data[idx]
                data[idx] = data[index]
                data[index] = temp
                idx++
            }
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
            Log.d("MomentPhotoWall", ""+helper.absoluteAdapterPosition)
            if (mContext == null) return
            val pos = helper.absoluteAdapterPosition
            val bg = helper.getView<ImageView>(R.id.bg)

            val filterView = helper.getView<ImageFilterView>(R.id.image)
            filterView.setImageResource(0)
            helper.itemView.setOnSingleClickListener({
                if (item.isEmpty()) {
                    this@EditPhotosWallActivity.bottomInBottomOut()
                        .add(R.id.root_layout, ChooseAlbumFragment().apply {
                            arguments = bundleOf("extra_mode" to Explorer.MODE_ONLY_IMAGE,
                                "item" to item)
                        }, "ChooseAlbumFragment").addToBackStack(null)
                        .commitAllowingStateLoss()
                } else {
                    DialogUtils.show(this@EditPhotosWallActivity, ReplaceDeleteDialog().apply {
                        onReplaceListener = object : ReplaceDeleteDialog.OnReplaceListener{
                            override fun onReplace() {
                                this@EditPhotosWallActivity.bottomInBottomOut()
                                    .add(R.id.root_layout, ChooseAlbumFragment().apply {
                                        arguments = bundleOf(
                                            "extra_mode" to Explorer.MODE_ONLY_IMAGE,
                                            "item" to item)
                                    }, "ChooseAlbumFragment").addToBackStack(null)
                                    .commitAllowingStateLoss()
                            }

                            override fun onDelete() {
                                item.albumOriginal = null
                                item.remoteFileId = null
                                reArrangeData()
                                adapter.notifyItemRangeChanged(0, 6)
                            }
                        }
                    })
                }
            }, 100)

            if (item.remoteFileId != null) {
                filterView.isVisible = true
                bg.isVisible = false
                Glide.with(mContext).load(getResourceIdFromFileId(item.remoteFileId!!)).into(filterView)
            } else if (item.albumOriginal != null) {
                filterView.isVisible = true
                bg.isVisible = false
                Glide.with(mContext).setDefaultRequestOptions(
                    RequestOptions.noAnimation().diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                ).load(item.albumOriginal).into(filterView)
            } else {
                filterView.isVisible = false
                bg.isVisible = true
            }
        }
    }

    /**
     * albumOriginal 在这个activity用户从相册原图本地数据， remoteFileId 进这个act前的后端数据。
     */
    data class Wrapper(var albumOriginal: String? = null,  var remoteFileId: String? = null) : BaseBean(){
        fun isEmpty(): Boolean{ //
            return albumOriginal == null && remoteFileId == null
        }

        override fun equals(other: Any?): Boolean {
            return other is Wrapper && albumOriginal == other.albumOriginal && remoteFileId == other.remoteFileId
        }
    }


    class CustomTouchCallback(val recyclerView: RecyclerView, val adapter: Adapter, val runnable: Runnable): ItemTouchHelper.Callback() {

        override fun getMovementFlags(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder
        ): Int {
            var idx = 6
            for (i in 0 until adapter.data.size) {
                if (adapter.data[i].isEmpty()) {
                    idx = i
                    break
                }
            }
            if (viewHolder.absoluteAdapterPosition >= idx) {
                return makeMovementFlags(0, 0)
            }
            return makeMovementFlags(ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT or ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0)
        }

        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            val fromPos = viewHolder.absoluteAdapterPosition
            val toPos = target.absoluteAdapterPosition
            var idx = 6
            for (i in 0 until adapter.data.size) {
                if (adapter.data[i].isEmpty()) {
                    idx = i
                    break
                }
            }

            if (fromPos >= idx || toPos >= idx) {
                return false
            }

            // 根据滑动方向 交换数据
            if (fromPos < toPos) {
                // 含头不含尾
                for (index in fromPos until toPos) {
                    Collections.swap(adapter.data, index, index + 1)
                }
            } else {
                // 含头不含尾
                for (index in fromPos downTo toPos + 1) {
                    Collections.swap(adapter.data, index, index - 1)
                }
            }
            adapter.notifyItemMoved(fromPos, toPos)
            runnable.run()
            return true
        }

        override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
            super.onSelectedChanged(viewHolder, actionState)
            if (actionState != ItemTouchHelper.ACTION_STATE_IDLE) {
                viewHolder!!.itemView.animate().setDuration(50).scaleX(1.15f).scaleY(1.15f).start()
            }
        }

        override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
            viewHolder!!.itemView.animate().setDuration(50).scaleX(1f).scaleY(1f).start()
            super.clearView(recyclerView, viewHolder)
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            //
        }
    }
}

//如果glide第2次加载图是resource, 第1次不管是resource/data 都从本地读
//如果glide第2次加载图是data, 第1次不管是只有是data 从本地读