package com.moment.app.images.media

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.alexvasilkov.gestures.views.GestureImageView
import com.moment.app.R
import com.moment.app.databinding.ActivityMediaPreviewLitBinding
import com.moment.app.images.Explorer
import com.moment.app.images.bean.MediaFile
import com.moment.app.images.engine.MediaStoreHelper
import com.moment.app.images.engine.OkDisplayCompat
import com.moment.app.images.media.adapter.MediaLimitChecker
import com.moment.app.images.utils.ExpUtil

class MediaPreviewActivity : AppCompatActivity(), IMediaContract.IMediaDataView {

    lateinit var binding: ActivityMediaPreviewLitBinding

    private var hideToolBar = false

    private val adapter by lazy {
        MediaPreviewAdapter(this)
    }

    private val loader by lazy {
        IMediaContract.MediaLoader(this,intent.extras,this)
    }

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
            )
        }
        super.onCreate(savedInstanceState)
        binding = ActivityMediaPreviewLitBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.explorerRecycler.adapter = adapter
        PagerSnapHelper().attachToRecyclerView(binding.explorerRecycler)
        if (Explorer.getRequest()?.getFileFilter()?.isSingleSelectMode() == true) {
            // 单图选择模式-不展示勾选框
            binding.explorerTvSelect.visibility = View.GONE
        } else {
            //test
            binding.explorerTvSelect.setOnClickListener {
                //选择、反选、边界
                if (adapter.itemCount == 0) return@setOnClickListener
                val file = adapter.getItem(getCurrentItem())?:return@setOnClickListener

                MediaLimitChecker.check(this@MediaPreviewActivity, file){
                    MediaStoreHelper.handleSelectLogic(file.path){selected ->
                        Explorer.getDataTrack()?.onSelect(file,selected)
                    }
                    setSelectedStatus(file)
                }
            }
        }

        binding.explorerRecycler.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState != RecyclerView.SCROLL_STATE_IDLE) return
                adapter.getItem(getCurrentItem())?.let {
                    setSelectedStatus(it)
                }
            }
        })

        binding.explorerBottomBtnBack.setOnClickListener {
            onBackPressed()
        }

        binding.explorerBottomBtnApply.setOnClickListener {
            if (Explorer.getRequest()?.getFileFilter()?.isSingleSelectMode() == true) {
                // 单图选择模式，直接选中当前文件
                val file = adapter.getItem(getCurrentItem())?:return@setOnClickListener
                MediaLimitChecker.check(this@MediaPreviewActivity, file){
                    MediaStoreHelper.handleSelectLogic(file.path){selected ->
                        Explorer.getDataTrack()?.onSelect(file,selected)
                    }
                    setSelectedStatus(file)
                }
            } else if (MediaStoreHelper.selectedFiles.isEmpty()) {
                adapter.getItem(getCurrentItem())?.let {
                    MediaStoreHelper.selectedFiles.add(it.path)
                }
            }
            //setResult给上级,MediaHelper持有选中数据，无需发送数据，此处等于通知
            val intent = Intent()
            setResult(RESULT_OK, intent)
            finish()
        }

        handleIntent()
        updateApplyStatus()
    }

    private fun updateApplyStatus() {
        when {
            Explorer.getRequest()?.getFileFilter()?.isSingleSelectMode() == true -> {
                // 单图选择模式-固定展示apply，可点击
                binding.explorerBottomBtnApply.isEnabled = true
                binding.explorerBottomBtnApply.text = getString(R.string.explorer_apply)
            }
            MediaStoreHelper.selectedFiles.isEmpty() -> { //不可点击，浅色
                binding.explorerBottomBtnApply.isEnabled = false
                binding.explorerBottomBtnApply.text = getString(R.string.explorer_apply)
            }
            else -> {
                binding.explorerBottomBtnApply.isEnabled = true
                binding.explorerBottomBtnApply.text =
                    getString(R.string.explorer_apply) + "(${MediaStoreHelper.selectedFiles.size})"
            }
        }
    }

    private var isPreviewMode = false
    private var clickedIndex = 0
    private var latestDirId = MediaStoreHelper.DIR_ID_ALL

    private fun handleIntent() {
        isPreviewMode = intent.getBooleanExtra(Explorer.EXTRA_PREVIEW_MODE, false)
        clickedIndex = intent.getIntExtra(Explorer.EXTRA_PREVIEW_INDEX, 0)
    }

    override fun onPause() {
        super.onPause()
        loader.release()
    }

    override fun onResume() {
        super.onResume()
        loader.fetchMediaData()
    }

    private fun handleData(){
        try {
            if (isPreviewMode) { //预览模式
                adapter.setData(
                    MediaStoreHelper.fetchSelectedFileList()
                        .filter { !it.isVideo } as MutableList<MediaFile>,true)
            } else {
                latestDirId = intent.getStringExtra(Explorer.EXTRA_PREVIEW_LATEST_ID) ?: MediaStoreHelper.DIR_ID_ALL
                //MediaHelper.directories.find { it.id == latestDirId }
                MediaStoreHelper.fetchFilesByDirId(latestDirId) { files, _ ->
                    if ((adapter.itemCount > 0 && clickedIndex == adapter.itemCount) || clickedIndex < 0) return@fetchFilesByDirId
                    val clickedFile = files[clickedIndex]
                    //暂时过滤掉视频，获取realIndex
                    val fileList = files.filter { !it.isVideo } as MutableList<MediaFile>
                    fileList.forEachIndexed { i, mediaFile ->
                        if (mediaFile.path == clickedFile.path) {
                            clickedIndex = i
                            return@forEachIndexed
                        }
                    }
                    adapter.setData(fileList, true)
                }

            }

            binding.explorerRecycler.scrollToPosition(clickedIndex)

            adapter.getItem(clickedIndex)?.let {
                setSelectedStatus(it)
            }
        }catch (e: Exception){
            Explorer.getDataTrack()?.onError("MediaPreviewActivity#handleData()", e)
        }

    }

    private fun getCurrentItem(): Int {
        return (binding.explorerRecycler.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
    }

    private fun setSelectedStatus(file: MediaFile) {
        if (MediaStoreHelper.selectedFiles.contains(file.path)) {
            val index = MediaStoreHelper.selectedFiles.indexOf(file.path) + 1
            binding.explorerTvSelect.textSize = if (index > 9) 12f else 14f
            binding.explorerTvSelect.text = "$index"
            binding.explorerTvSelect.isSelected = true
        } else {
            binding.explorerTvSelect.text = ""
            binding.explorerTvSelect.isSelected = false
        }
        ExpUtil.setTopPadding(binding.toolBar, this.applicationContext)
        updateApplyStatus()
    }

    inner class MediaPreviewAdapter(private val context: Context) :
        RecyclerView.Adapter<MediaViewHolder>() {

        private val imageList: MutableList<MediaFile> = mutableListOf()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediaViewHolder {

            val view = LayoutInflater.from(context)
                .inflate(R.layout.item_view_media_preview_pick, parent, false)

            return MediaViewHolder(view)

        }

        fun getItem(position: Int): MediaFile? {
            try {
                if (imageList.isEmpty()) return null
                if (position >= imageList.size || position < 0) return null
                return imageList[position]
            } catch (e: Exception) {
                return null
            }
        }

        fun setData(data: MutableList<MediaFile>, forceClear: Boolean = false) {

            if (data.size == 0) return

            if (forceClear) {
                imageList.clear()
            }

            imageList.addAll(data)
            notifyDataSetChanged()
        }

        override fun getItemCount(): Int = imageList.size

        override fun onBindViewHolder(holder: MediaViewHolder, position: Int) {
            holder.setData(imageList[position])
        }

    }


    inner class MediaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun setData(file: MediaFile) {
            try {
                OkDisplayCompat.load(itemView.findViewById(R.id.explorer_iv_media), file)
                itemView.findViewById<GestureImageView>(R.id.explorer_iv_media).setOnClickListener {
                    hideToolBar = !hideToolBar
                    binding.toolBar.visibility = if (hideToolBar) View.GONE else View.VISIBLE
                    binding.explorerBottomBar.visibility = if (hideToolBar) View.GONE else View.VISIBLE
                }
            } catch (ignore: Exception) {
            }
        }


    }

    override fun onRefreshData() {
        MediaStoreHelper.updateSelect()
        handleData()
    }
}