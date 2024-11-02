package com.moment.app.localimages.album.adapter

import android.content.Context
import android.graphics.Outline
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.moment.app.R
import com.moment.app.localimages.AlbumSearcher
import com.moment.app.localimages.datamodel.AlbumItemFile
import com.moment.app.localimages.logic.AlbumImageTask
import com.moment.app.localimages.logic.MediaStoreHelper
import com.moment.app.localimages.utils.Util
import java.lang.Exception

class AlbumAdapter(private val context: Context) :
    RecyclerView.Adapter<AlbumAdapter.AlbumViewHolder>() {

    private var size = context.resources.displayMetrics.widthPixels / AlbumSearcher.DEFAULT_SPAN_COUNT

    private val imageList: MutableList<AlbumItemFile> = mutableListOf()

    var selectAction: (() -> Unit)? = null

    var videoSelectAction: ((file: AlbumItemFile) -> Unit)? = null

    var latestDirId = "ALL"

    private var extras: Bundle? = null

    fun parseBundle(bundle: Bundle?) {
        this.extras = bundle
        bundle?.let {
            size = context.resources.displayMetrics.widthPixels / it.getInt("extra_span_count",
                AlbumSearcher.DEFAULT_SPAN_COUNT)
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

    private fun setData(data: MutableList<AlbumItemFile>, forceClear: Boolean = false) {
        if (data.size == 0) return

        if (forceClear) {
            imageList.clear()
        }

        imageList.addAll(data)
        notifyDataSetChanged()
    }

    private fun removeDirtyFile(file: AlbumItemFile) {
        MediaStoreHelper.removeDirtyFile(latestDirId, file) { files ->
            setData(files, true)
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlbumViewHolder {
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
                    outline?.setRoundRect(selfRect, Util.dp2px(4f, context).toFloat())
                }

            }
            view.findViewById<ImageView>(R.id.explorer_media_thumb_view).clipToOutline = true
        }

        return AlbumViewHolder(view)
    }

    override fun getItemCount(): Int = imageList.size

    override fun onBindViewHolder(holder: AlbumViewHolder, position: Int) {
        holder.setData(imageList[position])
    }

    inner class AlbumViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun setData(file: AlbumItemFile) {
            try {
                AlbumImageTask.loadThumb(itemView.findViewById<ImageView>(R.id.explorer_media_thumb_view), file, 0.3f, {
                    removeDirtyFile(it)
                })

                itemView.findViewById<View>(R.id.explorer_tv_select).setOnClickListener {
                    AlbumExaminer.check(context, file){
                        MediaStoreHelper.handleSelectLogic(file.path) { selected ->
                            AlbumSearcher.getDataTrack()?.onSelect(file, selected)
                        }
                        selectAction?.invoke()
                        notifyDataSetChanged()
                    }
                }

                itemView.setOnClickListener {
                    if (file.isVideo) {  //选择视频，最大支持阈值判断，超出toast提示，未超出直接返回
                        AlbumSearcher.videoPreview(context, file.path)
                        return@setOnClickListener
                    }

                    AlbumSearcher.mediaPreview(context as AppCompatActivity,
                        false,
                        adapterPosition,
                        latestDirId,
                        extras)
                }


                val isSelected = MediaStoreHelper.selectedFiles.contains(file.path)
                setSelectedStatus(isSelected, file)

                if (file.isVideo) {
                    itemView.findViewById<TextView>(R.id.explorer_tv_video_duration).text =
                        AlbumImageTask.formatTime(file.duration)
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

                if (AlbumSearcher.getRequest()?.getFileFilter()?.isSingleSelectMode() == true) {
                    // 如果是单图选择模式，不展示勾选框
                    itemView.findViewById<View>(R.id.explorer_tv_select).visibility = View.GONE
                }

            } catch (e: Exception) {
                AlbumSearcher.getDataTrack()?.onError("MediaAdapter#setData", e)
            }
        }

        private fun setSelectedStatus(isSelected: Boolean, file: AlbumItemFile) {
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