package com.moment.app.localimages.logic.loaders

import android.content.Context
import android.provider.MediaStore
import android.provider.MediaStore.Images.Media
import androidx.loader.content.CursorLoader

class PhotoGalleryLoader(context: Context) : CursorLoader(context) {


    companion object{


        private val IMAGE_PROJECTION = arrayOf(
                Media._ID,
                Media.DATA, Media.BUCKET_ID,
                Media.BUCKET_DISPLAY_NAME,
                Media.MIME_TYPE,
                MediaStore.Files.FileColumns.SIZE,
                Media.WIDTH,
                Media.HEIGHT,
                Media.DATE_ADDED)
    }



    init {
        projection = IMAGE_PROJECTION
        uri = Media.EXTERNAL_CONTENT_URI
        sortOrder = Media.DATE_MODIFIED + " DESC"
       // selection = MIME_TYPE + "=? or " + MIME_TYPE + "=? or " + MIME_TYPE + "=? or " + MIME_TYPE + "=? or " + MIME_TYPE + "=? " + if (showGif) "or $MIME_TYPE=?" else ""

       // selectionArgs = if (showGif) arrayOf(IMAGE_JPG, IMAGE_JPEG, IMAGE_PNG, IMAGE_BMP, IMAGE_WEBP, IMAGE_GIF) else arrayOf(IMAGE_JPG, IMAGE_JPEG, IMAGE_PNG, IMAGE_BMP, IMAGE_WEBP)

      //  selectionArgs = arrayOf(IMAGE_ALL)

    }


}