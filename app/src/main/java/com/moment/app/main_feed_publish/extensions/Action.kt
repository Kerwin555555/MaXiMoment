package com.moment.app.main_feed_publish.extensions

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import com.moment.app.localimages.datamodel.AlbumItemFile

sealed class Action {
    sealed class ImageAction() : Action()
    object AddImageAction: ImageAction() {
        var imageAlbumPosition =  0
        var file: AlbumItemFile? = null
    }
    object RemoveImageAction: ImageAction() {
        var imageAlbumPosition =  0   // 在album adapter pos
        var imageUploadPosition = 0 //在备选提交区域的adapter pos
        var file: AlbumItemFile? = null
    }
    object AddNewPhotoAction: ImageAction() {
        var uri: Uri? = null
    }
    object RemoveNewPhotoAction: ImageAction() {
        var photoAdapterPosition =  0    // 在备选提交区域的adapter pos如果从linkedhashmap中抽出照片的话，在照片中的 中的pos ,序列号 -1 -2 -3 -4 为了避开image
        var uri: Uri? = null
    }

    sealed class TextAction(): Action()
    object UpdateTextAction: TextAction() {
        var text:String? =null
    }
}

fun MutableLiveData<PostStatus>.reduce(action: Action) {
    val state = this.value!!

    state.latestImageAction = if (action is Action.ImageAction) action else null
    when (action) {
        is Action.AddImageAction -> {
            state.linkedHashMap[action.imageAlbumPosition] = action.file!!
        }
        is Action.RemoveImageAction -> {
            state.linkedHashMap.remove(action.imageAlbumPosition)
        } is Action.AddNewPhotoAction -> {
            state.newPhotos ++
            state.linkedHashMap[-state.newPhotos] = action.uri
        } is Action.RemoveNewPhotoAction -> {
            state.newPhotos--
            val itr = state.linkedHashMap.iterator()
            val pairs = mutableListOf<Pair<Int, Any?>>()
            var idx = 0
            while (itr.hasNext()) {
                val pair = itr.next()
                if (idx == action.photoAdapterPosition) {
                    itr.remove()
                    break
                }
                idx++
            }
            while (itr.hasNext()) {
                val p = itr.next()
                if (p.key < 0) {
                    pairs.add((p.key + 1 to p.value))
                } else {
                    pairs.add((p.key to p.value))
                }
                itr.remove()
            }
            for (i in pairs) {
                state.linkedHashMap[i.first] = i.second
            }
        } is Action.UpdateTextAction -> {
            state.editTextText = action.text
        }
    }
    this.value = state
}
