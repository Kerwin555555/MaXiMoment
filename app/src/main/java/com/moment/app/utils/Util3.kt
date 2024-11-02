package com.moment.app.utils

import android.net.Uri
import android.util.Log
import android.widget.ImageView
import androidx.constraintlayout.utils.widget.ImageFilterView
import com.github.iielse.imageviewer.adapter.ItemType.PHOTO
import com.github.iielse.imageviewer.core.Photo
import com.moment.app.image_viewer.show
import com.moment.app.localimages.datamodel.AlbumItemFile


fun ImageFilterView.showInImageViewer(dataList: List<ViewerPhoto>, clickedData: ViewerPhoto) {
    for (i in 0 until dataList.size) {
        dataList[i].pos = i.toLong()
    }
    show(dataList, clickedData)
}

fun ImageView.showInImageViewer(dataList: List<String>, clickedData: String) {
    kotlin.runCatching {
        val list = dataList.map { it ->
            ViewerPhoto.WallOrAvatarPhoto().apply {
                fileId = it
            }
        }
        var data: ViewerPhoto.WallOrAvatarPhoto? = null
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
            ViewerPhoto.WallOrAvatarPhoto().apply {
                fileId = it
            }
        }
        var data: ViewerPhoto.WallOrAvatarPhoto? = null
        for (i in 0 until dataList.size) {
            list[i].pos = i.toLong()
            if (list[i].fileId == clickedData) data = list[i]
        }
        show(list, data!!)
    }
}

fun ImageView.showAlbumInImageViewer(dataList: List<String>, clickedData: String, item: AlbumItemFile? = null) {
    kotlin.runCatching {
        val list = dataList.map { it ->
            Log.d(MOMENT_APP, item?.mimeType ?: "notype")
            ViewerPhoto.FeedAlbumFileIdPhoto().apply {
                fileId = it
                isGif = item?.mimeType == "image/gif"
            }
        }
        var data: ViewerPhoto.FeedAlbumFileIdPhoto? = null
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

    //头像，头像墙(非小图) (网络图)
    class WallOrAvatarPhoto : ViewerPhoto() {
        var fileId: String? = null
    }

    //相机 (本地图)
    class UriPhoto : ViewerPhoto() {
        var uri: Uri? = null
    }

    //feed流 (网络图)
    class PicShape(var fileKey: String, var width: Int? = null, var height: Int? = null): ViewerPhoto() {

        override fun id(): Long {
            return pos
        }

        override fun itemType(): Int {
            return PHOTO
        }
    }

    //发布feed页 (本地图)
    class FeedAlbumFileIdPhoto : ViewerPhoto() {
        var fileId: String? = null
        var isGif = false
    }
}