package com.moment.app.localimages.album

import android.content.Context
import android.widget.ListPopupWindow
import com.moment.app.localimages.datamodel.Album
import com.moment.app.localimages.logic.MediaStoreHelper
import com.moment.app.localimages.album.adapter.AlbumsAdapter

class AlbumSelectionPopup(context: Context) : ListPopupWindow(context) {

    private var directoryAdapter = AlbumsAdapter(context)

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

    fun getItem(position: Int): Album?{
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