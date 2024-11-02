package com.moment.app.main_profile_edit

import android.app.Application
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
import com.blankj.utilcode.util.SizeUtils
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.didi.drouter.annotation.Router
import com.gyf.immersionbar.ImmersionBar
import com.moment.app.R
import com.moment.app.databinding.ActivityEditWallPhotosBinding
import com.moment.app.eventbus.UpdateUserInfoEvent
import com.moment.app.localimages.AlbumSearcher
import com.moment.app.login_profile.ChooseAlbumFragment
import com.moment.app.login_profile.PictureCroppingView
import com.moment.app.login_profile.OnImageConfirmListener
import com.moment.app.main_profile_edit.dialogs.ReplaceDeleteDialog
import com.moment.app.models.UserLoginManager
import com.moment.app.network.startCoroutine
import com.moment.app.network.toast
import com.moment.app.user_rights.MomentActionDialog
import com.moment.app.user_rights.SetupBundle
import com.moment.app.utils.BaseActivity
import com.moment.app.utils.BaseBean
import com.moment.app.utils.DialogFragmentManager
import com.moment.app.utils.MOMENT_APP
import com.moment.app.utils.ProgressIndicatorFragment
import com.moment.app.utils.applyEnabledColorIntStateList
import com.moment.app.utils.applyMargin
import com.moment.app.utils.bottomInBottomOut
import com.moment.app.utils.cleanSaveFragments
import com.moment.app.utils.dp
import com.moment.app.utils.getScreenHeight
import com.moment.app.utils.getScreenWidth
import com.moment.app.utils.saveView
import com.moment.app.utils.setOnAvoidMultipleClicksListener
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
        binding.save.applyEnabledColorIntStateList(enableId =
           0xff1d1d1d.toInt() , disableId = 0xffE5E5E5.toInt())
        binding.save.isEnabled = false
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
            val progresDialog = ProgressIndicatorFragment.show(this@EditPhotosWallActivity)
            progresDialog.isCancelable = false
            startCoroutine({
                val list = mutableListOf<Deferred<String?>>()
                val w = getScreenWidth()
                val h = getScreenHeight()
                for (item in adapter.data) {
                    if (item.remoteFileId != null) {
                        list.add(this.async(Dispatchers.IO) {
                            item.remoteFileId
                        })
                    } else if (item.albumOriginal != null){
                        list.add(this.async(Dispatchers.IO) {
                            val bitmap = Glide.with(this@EditPhotosWallActivity).asBitmap().load(item.albumOriginal).centerInside().submit(w, h).get()
                            saveView(this@EditPhotosWallActivity, bitmap)?.absolutePath ?: ""
                        })
                    }
                }
                val result = list.awaitAll()
                delay(400) //mock upload to cloud and backend
                UserLoginManager.setUserInfo(UserLoginManager.getUserInfo()?.apply {
                    val mutableList = mutableListOf<String>()
                    result.forEach { fileid->
                        mutableList.add(fileid!!)
                    }
                    imagesWallList = mutableList
                    avatar = imagesWallList[0]
                })
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

        adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver(){
            override fun onItemRangeChanged(positionStart: Int, itemCount: Int) {
                super.onItemRangeChanged(positionStart, itemCount)
                binding.save.isEnabled = hasChangeComparedToInit()
            }

            override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) {
                super.onItemRangeMoved(fromPosition, toPosition, itemCount)
                binding.save.isEnabled = hasChangeComparedToInit()
            }
        })

        (binding.rv.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false //remove shimmer
    }

    override fun onBackPressed() {
        if (hasChangeComparedToInit()) {
            showSaveReminderDialog()
            return
        }
        super.onBackPressed()
    }

    private fun hasChangeComparedToInit(): Boolean {
        for (i in 0 until initialList!!.size) {
            if (adapter.data[i].remoteFileId != initialList?.get(i)) {
                return true
            }
        }
        for (i in initialList!!.size until 6) {
            if (!adapter.data[i].isEmpty()) {
                return true
            }
        }
        return false
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

    override fun onConfirm(clipImageView: PictureCroppingView, map: Map<String, Any?>?) {
        kotlin.runCatching {
            supportFragmentManager.let {
                val f = it.findFragmentByTag("CroppingPictureFragment")
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
                map["file"]?.let { f ->
                    item.albumOriginal = f
                } ?: let {
                    map["uri"]?.let { uri ->
                        item.albumOriginal = uri
                    }
                }
                Log.d(MOMENT_APP, "dddd:"+ item.albumOriginal.toString())
                item.remoteFileId = null
                reArrangeData()
                adapter.notifyItemRangeChanged(0, 6)
            } ?: let {

            }
        }
        //Glide.with(this).load("xxx").fitCenter().submit(ScreenUtils.getAppScreenWidth(), ScreenUtils.getAppScreenHeight()).get()

    }

    private fun getNonEmptyCount(): Int {
        var count = 0
        for (i in adapter.data) {
            if (!i.isEmpty()) count++
        }
        return count
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
            helper.itemView.setOnAvoidMultipleClicksListener({
                if (item.isEmpty()) {
                    this@EditPhotosWallActivity.bottomInBottomOut()
                        .add(R.id.root_layout, ChooseAlbumFragment().apply {
                            arguments = bundleOf("extra_mode" to AlbumSearcher.MODE_ONLY_IMAGE,
                                "item" to item)
                        }, "ChooseAlbumFragment").addToBackStack(null)
                        .commitAllowingStateLoss()
                } else {
                    DialogFragmentManager.show(this@EditPhotosWallActivity, ReplaceDeleteDialog().apply {
                        arguments = bundleOf("hideDelete" to (getNonEmptyCount() == 1))
                        onReplaceListener = object : ReplaceDeleteDialog.OnReplaceListener{
                            override fun onReplace() {
                                this@EditPhotosWallActivity.bottomInBottomOut()
                                    .add(R.id.root_layout, ChooseAlbumFragment().apply {
                                        arguments = bundleOf(
                                            "extra_mode" to AlbumSearcher.MODE_ONLY_IMAGE,
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
                Glide.with(mContext).load(item.remoteFileId!!).into(filterView)
            } else if (item.albumOriginal != null) {
                filterView.isVisible = true
                bg.isVisible = false
                Glide.with(mContext).asBitmap().load(item.albumOriginal)
                    .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                    .into(filterView)
            } else {
                filterView.isVisible = false
                bg.isVisible = true
            }
        }
    }

    /**
     * albumOriginal 在这个activity用户从相册原图本地数据 可以为fileid,也可以是uri， remoteFileId 进这个act前的后端数据。
     */
    data class Wrapper(var albumOriginal: Any? = null,  var remoteFileId: String? = null) : BaseBean(){
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
            viewHolder.itemView.animate().setDuration(50).scaleX(1f).scaleY(1f).start()
            super.clearView(recyclerView, viewHolder)
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            //
        }
    }
}
//skipmemcache(true) 前提
//如果glide第1次resource (key"urlxxx"), 第二次resource(key"urlxyy")/data(key"url) 依然远程
//(但是 resource "key "urlxxx" 可以本地，而 data "key  urlxxx" 不可以 本地)
//如果glide第1次加载图是data (key"url"), 第2次是data (key"url" 存在)从本地读,
//RESOURCE (key"urlxxx" 不从本地,依然远程)
//Glide.with(mContext)
//.load("https://pic1.zhimg.com/v2-a6b58e82c8cdb830ad7bd85d86469458_l.jpg?source=172ae18b")
//.centerInside().override(89, 89)
//.diskCacheStrategy(DiskCacheStrategy.RESOURCE)
//.skipMemoryCache(true)
//.into(filterView)