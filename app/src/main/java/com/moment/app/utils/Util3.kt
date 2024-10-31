package com.moment.app.utils

import android.net.Uri
import android.widget.ImageView
import androidx.constraintlayout.utils.widget.ImageFilterView
import com.github.iielse.imageviewer.adapter.ItemType.PHOTO
import com.github.iielse.imageviewer.core.Photo
import com.moment.app.image_viewer.show


fun ImageFilterView.showInImageViewer(dataList: List<ViewerPhoto>, clickedData: ViewerPhoto) {
    for (i in 0 until dataList.size) {
        dataList[i].pos = i.toLong()
    }
    show(dataList, clickedData)
}

fun ImageView.showInImageViewer(dataList: List<String>, clickedData: String) {
    kotlin.runCatching {
        val list = dataList.map { it ->
            ViewerPhoto.FileIdPhoto().apply {
                fileId = it
            }
        }
        var data: ViewerPhoto.FileIdPhoto? = null
        for (i in 0 until dataList.size) {
            list[i].pos = i.toLong()
            if (list[i].fileId == clickedData) data = list[i]
        }
        show(list, data!!)
    }
}

fun ImageView.showInImageViewer(dataList: List<Uri>, clickedData: Uri) {
    kotlin.runCatching {
        val list = dataList.map { it ->
            ViewerPhoto.UriPhoto().apply {
                uri = it
            }
        }
        var data: ViewerPhoto.UriPhoto? = null
        for (i in 0 until dataList.size) {
            list[i].pos = i.toLong()
            if (list[i].uri == clickedData) data = list[i]
        }
        show(list, data!!)
    }
}

fun ImageFilterView.showInImageViewer(dataList: List<String>, clickedData: String) {
    kotlin.runCatching {
        val list = dataList.map { it ->
            ViewerPhoto.FileIdPhoto().apply {
                fileId = it
            }
        }
        var data: ViewerPhoto.FileIdPhoto? = null
        for (i in 0 until dataList.size) {
            list[i].pos = i.toLong()
            if (list[i].fileId == clickedData) data = list[i]
        }
        show(list, data!!)
    }
}




sealed class ViewerPhoto : BaseBean(), Photo {
    var pos: Long = 0

    override fun id(): Long {
        return pos
    }

    override fun itemType(): Int {
        return PHOTO
    }

    //头像或者 feed发布时候来自相册的大图
    class FileIdPhoto : ViewerPhoto() {
        var fileId: String? = null
    }

    //相机
    class UriPhoto : ViewerPhoto() {
        var uri: Uri? = null
    }

    //feed流
    class PicShape(var fileKey: String, var width: Int? = null, var height: Int? = null): ViewerPhoto() {

        override fun id(): Long {
            return pos
        }

        override fun itemType(): Int {
            return PHOTO
        }
    }
}