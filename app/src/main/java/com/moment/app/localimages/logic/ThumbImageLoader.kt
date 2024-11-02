package com.moment.app.localimages.logic

import android.content.Context
import android.graphics.Bitmap
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.util.LruCache
import com.moment.app.localimages.datamodel.AlbumItemFile
import java.lang.ref.SoftReference

object ThumbImageLoader {

    private val thumbnailsCache = LruCache<String, SoftReference<Bitmap>>(1024)

    fun fetchImageThumb(
        context: Context,
        file: AlbumItemFile,
        action: (bitmap: Bitmap?) -> Unit,
        error: (file: AlbumItemFile) -> Unit
    ) {
        if (thumbnailsCache.get(file.path) != null && thumbnailsCache[file.path]?.get() != null) {
            Log.d("OKThumbLoader", "load from memory cache...")
            action.invoke(thumbnailsCache[file.path]?.get())
            return
        }

        ThreadTackler.push {
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