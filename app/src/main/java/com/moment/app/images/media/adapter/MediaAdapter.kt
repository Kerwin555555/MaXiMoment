package com.moment.app.images.media.adapter

import android.content.Context
import android.content.Intent
import android.graphics.Outline
import android.graphics.Rect
import android.media.Image
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.moment.app.R
import com.moment.app.images.Explorer
import com.moment.app.images.bean.MediaFile
import com.moment.app.images.engine.OkDisplayCompat
import com.moment.app.images.engine.MediaStoreHelper
import com.moment.app.images.utils.ExpUtil
import java.lang.Exception

class MediaAdapter(private val context: Context) :
    RecyclerView.Adapter<MediaAdapter.MediaViewHolder>() {

    private var size = context.resources.displayMetrics.widthPixels / Explorer.DEFAULT_SPAN_COUNT

    private val imageList: MutableList<MediaFile> = mutableListOf()

    var selectAction: (() -> Unit)? = null

    var videoSelectAction: ((file: MediaFile) -> Unit)? = null

    var latestDirId = "ALL"

    private var extras: Bundle? = null

    fun parseBundle(bundle: Bundle?) {
        this.extras = bundle
        bundle?.let {
            size = context.resources.displayMetrics.widthPixels / it.getInt("extra_span_count",
                Explorer.DEFAULT_SPAN_COUNT)
        }
    }

    fun update() {
        selectDirectoryById(latestDirId)
    }

    fun selectDirectoryById(id: String) {
        MediaStoreHelper.fetchFilesByDirId(id) { files, latestDirId ->
            setData(files, true)
            this.latestDirId = latestDirId
        }
    }

    private fun setData(data: MutableList<MediaFile>, forceClear: Boolean = false) {
        if (data.size == 0) return

        if (forceClear) {
            imageList.clear()
        }

        imageList.addAll(data)
        notifyDataSetChanged()
    }

    private fun removeDirtyFile(file: MediaFile) {
        MediaStoreHelper.removeDirtyFile(latestDirId, file) { files ->
            setData(files, true)
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediaViewHolder {
        val view =
            LayoutInflater.from(context).inflate(R.layout.item_view_media_pick, parent, false)
                .also {
                    it.layoutParams = RecyclerView.LayoutParams(size, size)
                }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            view.findViewById<ImageView>(R.id.explorer_media_thumb_view).outlineProvider = object : ViewOutlineProvider(){
                override fun getOutline(target: View?, outline: Outline?) {
                    val rect = Rect()
                    target?.getGlobalVisibleRect(rect);
                    val selfRect = Rect(0, 0,
                        target!!.width, target.height);
                    //这里还可以使用setRect()矩形  setOval()圆形
                    outline?.setRoundRect(selfRect, ExpUtil.dp2px(4f, context).toFloat())
                }

            }
            view.findViewById<ImageView>(R.id.explorer_media_thumb_view).clipToOutline = true
        }

        return MediaViewHolder(view)
    }

    override fun getItemCount(): Int = imageList.size

    override fun onBindViewHolder(holder: MediaViewHolder, position: Int) {
        holder.setData(imageList[position])
    }

    inner class MediaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun setData(file: MediaFile) {
            try {
                OkDisplayCompat.loadThumb(itemView.findViewById<ImageView>(R.id.explorer_media_thumb_view), file, 0.3f, {
                    removeDirtyFile(it)
                })

                itemView.findViewById<View>(R.id.explorer_tv_select).setOnClickListener {
                    MediaLimitChecker.check(context, file){
                        MediaStoreHelper.handleSelectLogic(file.path) { selected ->
                            Explorer.getDataTrack()?.onSelect(file, selected)
                        }
                        selectAction?.invoke()
                        notifyDataSetChanged()
                    }
                }

                itemView.setOnClickListener {
                    if (file.isVideo) {  //选择视频，最大支持阈值判断，超出toast提示，未超出直接返回
                        Explorer.videoPreview(context, file.path)
                        return@setOnClickListener
                    }

                    Explorer.mediaPreview(context as AppCompatActivity,
                        false,
                        adapterPosition,
                        latestDirId,
                        extras)
                }


                val isSelected = MediaStoreHelper.selectedFiles.contains(file.path)
                setSelectedStatus(isSelected, file)

                if (file.isVideo) {
                    itemView.findViewById<TextView>(R.id.explorer_tv_video_duration).text =
                        OkDisplayCompat.formatTime(file.duration)
                    itemView.findViewById<TextView>(R.id.explorer_tv_video_duration).visibility = if (file.duration > 0 && file.size > 0) View.VISIBLE else View.GONE
                } else {
                    itemView.isEnabled = true
                    itemView.findViewById<View>(R.id.explorer_tv_select).visibility = View.VISIBLE
                    itemView.findViewById<TextView>(R.id.explorer_tv_video_duration).text = ""
                    itemView.findViewById<View>(R.id.explorer_media_thumb_view_overlay).visibility = View.GONE
                    if(file.isGif) {
                        itemView.findViewById<View>(R.id.explorer_gif_badge).visibility =View.VISIBLE
                    }
                }

                if (Explorer.getRequest()?.getFileFilter()?.isSingleSelectMode() == true) {
                    // 如果是单图选择模式，不展示勾选框
                    itemView.findViewById<View>(R.id.explorer_tv_select).visibility = View.GONE
                }

            } catch (e: Exception) {
                Explorer.getDataTrack()?.onError("MediaAdapter#setData", e)
            }
        }

        private fun setSelectedStatus(isSelected: Boolean, file: MediaFile) {
            if (isSelected) {
                val index = MediaStoreHelper.selectedFiles.indexOf(file.path) + 1
                itemView.findViewById<TextView>(R.id.explorer_tv).textSize = if (index > 9) 12f else 14f
                itemView.findViewById<TextView>(R.id.explorer_tv).text = "$index"
                itemView.findViewById<View>(R.id.explorer_tv_select).isSelected = true
            } else {
                itemView.findViewById<TextView>(R.id.explorer_tv).text = ""
                itemView.findViewById<View>(R.id.explorer_tv_select).isSelected = false
            }
            if (MediaStoreHelper.isUpToMaxPickCount() && !isSelected) {
                itemView.findViewById<ImageView>(R.id.explorer_media_thumb_view).alpha = 0.3f
            } else {
                itemView.findViewById<ImageView>(R.id.explorer_media_thumb_view).alpha = 1f
            }
        }
    }
}