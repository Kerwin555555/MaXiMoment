package com.moment.app.images.media.adapter

import android.content.Context
import android.widget.Toast
import com.moment.app.R
import com.moment.app.images.Explorer
import com.moment.app.images.bean.MediaFile
import com.moment.app.images.engine.MediaStoreHelper
import java.lang.Exception
import kotlin.math.roundToInt

object MediaLimitChecker {

    fun check(context: Context, file: MediaFile, selectAction: () ->Unit){
        val filter = Explorer.getRequest()?.getFileFilter() ?: return
        try {
            //选择、反选、边界
            val isVideoSelected =  MediaStoreHelper.fetchSelectedFileList().filter { it.isVideo }.toList().isNotEmpty()
            if (!filter.enableSelectMultipleType()) {
                if (isVideoSelected){ //设置视频和图片互斥
                    if (!file.isVideo){
                        Toast.makeText(context, R.string.video_without_photo, Toast.LENGTH_SHORT).show()
                        return
                    }
                }else{
                    if (MediaStoreHelper.selectedFiles.size > 0 && file.isVideo){
                        Toast.makeText(context, R.string.photo_without_video, Toast.LENGTH_SHORT).show()
                        return
                    }
                }
            }
            if (filter.maxPickCountTrigger(file)){
                val text = context.getString(if (isVideoSelected) R.string.video_select_count_limit else R.string.photo_select_count_limit)
                Toast.makeText(context,String.format(text, Explorer.getRequest()?.pickCount), Toast.LENGTH_SHORT).show()
            }else{
                if (file.isVideo) {
                    val maxVideoDuration = Explorer.getRequest()?.maxVideoDuration?:0
                    val maxVideoSize = Explorer.getRequest()?.maxVideoSize?:0.0
                    if(filter.maxVideoDurationTrigger(file)){
                        Toast.makeText(context, String.format(context.getString(R.string.video_max_seconds_limit), maxVideoDuration), Toast.LENGTH_SHORT).show()
                        return
                    }
                    if (filter.maxVideoSizeTrigger(file)){
                        Toast.makeText(context, String.format(context.getString(R.string.video_max_size_limit), "${maxVideoSize.roundToInt()} MB"), Toast.LENGTH_SHORT).show()
                        return
                    }
                }else{

                }

                selectAction.invoke()
            }
        }catch (e: Exception){
            Explorer.getDataTrack()?.onError("MediaLimitChecker#check()", e)
        }

    }
}