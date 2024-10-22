package com.moment.app.images.media

import android.content.Context
import android.widget.ListPopupWindow
import com.moment.app.images.bean.MediaDirectory
import com.moment.app.images.engine.MediaStoreHelper
import com.moment.app.images.media.adapter.MediaDirectoryAdapter

class MediaDirectoryWindow(context: Context) : ListPopupWindow(context) {

    private var directoryAdapter = MediaDirectoryAdapter(context)

    init {
        setAdapter(directoryAdapter)
        directoryAdapter.changeSelect(0)
        val sWidthPix = context.resources.displayMetrics.widthPixels
        setContentWidth(sWidthPix)
        height = sWidthPix
        width = (200 * context.resources.displayMetrics.density + 0.5f).toInt()
        isModal = true
        animationStyle = android.R.style.Animation_Dialog
      //  horizontalOffset = (-48 * context.resources.displayMetrics.density + 0.5f).toInt()
        verticalOffset = (-14 * context.resources.displayMetrics.density + 0.5f).toInt()
    }

    private fun setData() {
        directoryAdapter.setData(MediaStoreHelper.directories)
    }

    fun getItem(position: Int): MediaDirectory ?{
        return directoryAdapter.getItem(position)
    }

    fun setSelectedIndex(position: Int) {
        this.directoryAdapter.changeSelect(position)
    }

    override fun show() {
        if (isShowing)
            return

        setData()
        super.show()
        setSelection(directoryAdapter.selectedPosition)
    }

}