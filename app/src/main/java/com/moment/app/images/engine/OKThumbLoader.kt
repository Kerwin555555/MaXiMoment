package com.moment.app.images.engine

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.media.ThumbnailUtils
import android.net.Uri
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.util.LruCache
import com.moment.app.images.bean.MediaFile
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.FileNotFoundException
import java.io.IOException
import java.lang.Exception
import java.lang.ref.SoftReference

object OKThumbLoader {

    private val thumbnailsCache = LruCache<String, SoftReference<Bitmap>>(1024)

    fun fetchImageThumb(
        context: Context,
        file: MediaFile,
        action: (bitmap: Bitmap?) -> Unit,
        error: (file: MediaFile) -> Unit
    ) {
        if (thumbnailsCache.get(file.path) != null && thumbnailsCache[file.path]?.get() != null) {
            Log.d("OKThumbLoader", "load from memory cache...")
            action.invoke(thumbnailsCache[file.path]?.get())
            return
        }

        OkExecutor.push {
            Log.d("OKThumbLoader", "load from task...")
            var bitmap = if (file.isVideo) {
                MediaStore.Video.Thumbnails.getThumbnail(
                    context.contentResolver,
                    file.fileId,
                    MediaStore.Video.Thumbnails.MINI_KIND,
                    null
                )
            } else {
                MediaStore.Images.Thumbnails.getThumbnail(
                    context.contentResolver,
                    file.fileId,
                    MediaStore.Video.Thumbnails.MINI_KIND,
                    null
                )
            }

            if (bitmap == null) { //非视频，本地无缩虐图，取原图
                notify {
                    error.invoke(file)
                }
                return@push
            }

            notify {
                thumbnailsCache.put(file.path, SoftReference(bitmap))
                action.invoke(bitmap)
            }
        }
    }


    private fun notify(runnable: Runnable) {
        android.os.Handler(Looper.getMainLooper()).post(runnable)
    }
}