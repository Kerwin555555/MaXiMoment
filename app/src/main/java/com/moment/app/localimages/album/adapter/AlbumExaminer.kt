package com.moment.app.localimages.album.adapter

import android.content.Context
import android.widget.Toast
import com.moment.app.R
import com.moment.app.localimages.AlbumSearcher
import com.moment.app.localimages.datamodel.AlbumItemFile
import com.moment.app.localimages.logic.MediaStoreHelper
import java.lang.Exception
import kotlin.math.roundToInt

object AlbumExaminer {

    fun check(context: Context, file: AlbumItemFile, selectAction: () ->Unit){
        val filter = AlbumSearcher.getRequest()?.getFileFilter() ?: return
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
                Toast.makeText(context,String.format(text, AlbumSearcher.getRequest()?.pickCount), Toast.LENGTH_SHORT).show()
            }else{
                if (file.isVideo) {
                    val maxVideoDuration = AlbumSearcher.getRequest()?.maxVideoDuration?:0
                    val maxVideoSize = AlbumSearcher.getRequest()?.maxVideoSize?:0.0
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
            AlbumSearcher.getDataTrack()?.onError("MediaLimitChecker#check()", e)
        }

    }
}