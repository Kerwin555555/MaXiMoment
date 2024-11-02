package com.moment.app.localimages.logic

import android.content.Context
import android.database.Cursor
import com.moment.app.R
import com.moment.app.localimages.AlbumSearcher
import com.moment.app.localimages.datamodel.Album
import com.moment.app.localimages.datamodel.AlbumItemFile

/**
 * 统一管理数据源
 * 数据持久化
 */
object MediaStoreHelper {

    const val DIR_ID_ALL = "ALL"

    //数据源管理,防止数据混乱
    val directories: MutableList<Album> = mutableListOf()

    //选中的文件
    val selectedFiles: MutableList<String> = mutableListOf()

    fun parseMediaData(context: Context, cursor: Cursor) {
        val data = CursorParseHelper.parseImagesFromCursor(context, cursor)
        if (data.isEmpty() || data[0].files.size == 0) return
        directories.clear()
        directories.addAll(data)
    }


    fun parseVideoData(context: Context, cursor: Cursor,onlyVideo:Boolean = false) {
        if (onlyVideo){
            directories.clear()
        }
        val videoDirectory = CursorParseHelper.parseVideosFromCursor(context, cursor)
        if (directories.size == 0) {
            directories.add(Album().also {
                it.id = "ALL"
                it.name = context.resources.getString(R.string.explorer_all_media)
            })
        }
        if (videoDirectory.files.size > 0) {
            directories[0].apply {
                files.addAll(videoDirectory.files)
                files.sortByDescending { file ->
                    file.date
                }
            }
        }
    }

    /**
     * 处理选中文件的逻辑
     */
    fun handleSelectLogic(path: String, action:(isSelected:Boolean) ->Unit) {
        val maxSelectNum = AlbumSearcher.getRequest()?.pickCount
        val isSelected = selectedFiles.contains(path)
        if (isSelected) {
            selectedFiles.remove(path)
            action.invoke(false)
        } else {
            if (selectedFiles.size == maxSelectNum) return
            selectedFiles.add(path)
            action.invoke(true)
        }
    }

    /**
     * 更新选中
     */
    fun updateSelect() {
        val iterator = selectedFiles.iterator()
        while (iterator.hasNext()) {
            val path = iterator.next()
            val file = directories[0].files.find { it.path == path }
            if (file == null) {
                iterator.remove()
            }
        }
    }

    /**
     * 通过id获取media file文件
     */
    fun fetchFilesByDirId(id: String, callback: (files: MutableList<AlbumItemFile>, latestDirId: String) -> Unit) {
        directories.find { it.id == id }?.let {
            callback.invoke(it.files, id)
        }
    }

    /**
     * 获取选择的文件列表
     */
    fun fetchSelectedFileList(): MutableList<AlbumItemFile> {
        val realSelectedFile = mutableListOf<AlbumItemFile>()
        selectedFiles.forEach { path ->
            if (directories.size > 0){
                directories[0].files.find { f -> f.path == path }?.let { realSelectedFile.add(it) }
            }
        }
        return realSelectedFile
    }

    /**
     * 移除错误的文件
     */
    fun removeDirtyFile(id:String, file: AlbumItemFile, callback: (files: MutableList<AlbumItemFile>) -> Unit){
        directories.find { it.id == id && !file.isVideo}?.let {
            val iterator = it.files.iterator()
            while (iterator.hasNext()){
                val element = iterator.next()
                if (element.path.equals(file.path) ){
                    iterator.remove()
                }
            }
            callback.invoke(it.files)
        }
    }

    /**
     * 是否已经达到勾选上限
     */
    fun isUpToMaxPickCount(): Boolean{
        val request = AlbumSearcher.getRequest()
        return if (request != null) {
            selectedFiles.size >= request.pickCount
        } else {
            false
        }
    }

    /**
     * 退出释放数据
     */
    fun release() {
        directories.clear()
        selectedFiles.clear()
    }

}