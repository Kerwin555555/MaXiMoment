package com.moment.app.localimages.logic

import android.app.Activity
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.moment.app.localimages.AlbumSearcher
import com.moment.app.localimages.datamodel.AlbumItemFile
import java.text.DecimalFormat

object AlbumImageTask {

    fun loadThumb(targetView: ImageView, file: AlbumItemFile, sizeMultiplier: Float, dirtyFileAction: ((file: AlbumItemFile) ->Unit) ?= null, placeHolder: Drawable = ColorDrawable(0xFFEEEEEE.toInt())) {
        if (Build.VERSION.SDK_INT >= 29) {
            targetView.setImageDrawable(placeHolder)
            ThumbImageLoader.fetchImageThumb(targetView.context, file, { bitmap ->
                targetView.setImageBitmap(bitmap)
            }, {
                load(targetView, file, sizeMultiplier, dirtyFileAction, placeHolder)
            })
        } else {
            load(targetView, file, sizeMultiplier, dirtyFileAction, placeHolder)
        }
    }

    fun load(targetView: ImageView, file: AlbumItemFile, sizeMultiplier: Float = 0.3f, dirtyFileAction: ((file: AlbumItemFile) ->Unit) ?= null, placeHolder: Drawable = ColorDrawable(0xFF000000.toInt())) {
        targetView.setImageDrawable(placeHolder)
        val context = targetView.context?:return
        if (context is Activity && context.isDestroyed) return
        val defaultOptions =  if (file.isGif) {
            val filter = AlbumSearcher.getRequest()?.getFileFilter()
            if (filter != null && !filter.maxPreviewGifSizeTrigger(file)) {
                RequestOptions().diskCacheStrategy(DiskCacheStrategy.NONE)
            } else {
                RequestOptions.noAnimation().diskCacheStrategy(DiskCacheStrategy.NONE)
            }
        } else {
            RequestOptions.noAnimation().diskCacheStrategy(DiskCacheStrategy.RESOURCE)
        }
        Glide.with(context)
            .setDefaultRequestOptions(defaultOptions)
            .load(if (file.isVideo) file.path else file.displayPath())
            .dontTransform()
            .placeholder(placeHolder)
            .thumbnail(sizeMultiplier)
            .centerInside()
            .timeout(3000)
            .error(placeHolder)
            .addListener(object : RequestListener<Drawable>{
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>,
                    isFirstResource: Boolean
                ): Boolean {
                    dirtyFileAction?.invoke(file)
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable,
                    model: Any,
                    target: Target<Drawable>?,
                    dataSource: DataSource,
                    isFirstResource: Boolean
                ): Boolean {
                    return false
                }

            })
            .into(targetView)
    }


    fun formatTime(timeMs: Long): String {
        var timeMs = timeMs
        if (timeMs == Long.MAX_VALUE + 1) timeMs = 0.toLong()
        val totalSeconds = (timeMs + 500) / 1000
        val seconds = totalSeconds % 60
        val minutes = totalSeconds / 60 % 60
        val hours = totalSeconds / 3600

        return if (hours > 0)
            String.format("%d:%02d:%02d", hours, minutes, seconds)
        else
            String.format("%02d:%02d", minutes, seconds)
    }

    fun formatFileSize(fileS: Long): String? {
        val df = DecimalFormat("#.00")
        var fileSizeString = ""
        fileSizeString = when {
            fileS == 0L -> {
                "0.00B"
            }
            fileS < 1024L -> {
                df.format(fileS.toDouble()) + "B"
            }
            fileS < 1048576L -> {
                df.format(fileS.toDouble() / 1024.0) + "KB"
            }
            fileS < 1073741824L -> {
                df.format(fileS.toDouble() / 1048576.0) + "MB"
            }
            else -> {
                df.format(fileS.toDouble() / 1.073741824E9) + "G"
            }
        }
        return fileSizeString
    }

}