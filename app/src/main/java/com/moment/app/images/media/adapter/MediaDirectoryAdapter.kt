package com.moment.app.images.media.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.moment.app.R
import com.moment.app.images.bean.MediaDirectory
import com.moment.app.images.engine.OkDisplayCompat

class MediaDirectoryAdapter(val context: Context) : BaseAdapter() {

    private val photoDirectories: MutableList<MediaDirectory> = mutableListOf()
    var selectedPosition: Int = 0

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View? {
        var holder: ViewHolder?
        var view = convertView
        if (view == null) {
            view = LayoutInflater.from(context)
                .inflate(R.layout.item_view_media_directory, parent, false)
            holder = ViewHolder(view)
            view.tag = holder
        } else {
            holder = view.tag as ViewHolder?
        }
        holder?.setData(photoDirectories[position], selectedPosition == position)

        return view
    }

    fun setData(data: MutableList<MediaDirectory>) {
        if (data.isEmpty()) return
        this.photoDirectories.clear()
        this.photoDirectories.addAll(data)
        notifyDataSetChanged()
    }

    override fun getItem(position: Int): MediaDirectory? {
        try {
            if (photoDirectories.isEmpty()) return null
            if (position >= photoDirectories.size || position < 0) return null
            return photoDirectories[position]
        } catch (e: Exception) {
            return null
        }
    }

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getCount(): Int {
        return photoDirectories.size
    }

    fun changeSelect(position: Int) {
        selectedPosition = position
        notifyDataSetChanged()
    }

    class ViewHolder(private val itemView: View) {

        fun setData(directory: MediaDirectory, selected: Boolean) {
            try {
                itemView.findViewById<TextView>(R.id.explorer_tv_directory_name).text = directory.name
                itemView.findViewById<TextView>(R.id.explorer_tv_directory_element_count).text = "${directory.photoPaths.size}"

//                if (selected) {

//                } else {
//
//                }

                val file = directory.files[0] ?: return
//                if (file.isVideo) {
//
//                } else {
//
//                }

                OkDisplayCompat.loadThumb(itemView.findViewById<ImageView>(R.id.explorer_iv_directory_cover), file, 0.3f)

            } catch (e: Exception) {

            }

        }
    }

}