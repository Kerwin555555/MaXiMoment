package com.moment.app.images.engine.data

import android.content.Context
import android.provider.MediaStore
import androidx.loader.content.CursorLoader

class VideoCursorLoader(context: Context) : CursorLoader(context) {

    companion object{

        val VIDEO_PROJECT = arrayOf(
                MediaStore.Video.Media._ID,
                MediaStore.Video.Media.ALBUM,
                MediaStore.Video.Media.DATE_ADDED,
                MediaStore.Video.Media.DURATION,
                MediaStore.Video.Media.DATA,
                MediaStore.Video.Media.SIZE,
                MediaStore.Video.Media.MIME_TYPE,
                MediaStore.Video.Media.WIDTH,
                MediaStore.Video.Media.HEIGHT,
                MediaStore.Video.Media.TITLE)

    }

    init {
        projection = VIDEO_PROJECT
        uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        sortOrder = MediaStore.Video.Media.DATE_ADDED + " DESC"

    }

}