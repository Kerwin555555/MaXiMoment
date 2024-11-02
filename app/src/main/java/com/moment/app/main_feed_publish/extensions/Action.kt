package com.moment.app.main_feed_publish.extensions

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import com.moment.app.localimages.datamodel.AlbumItemFile

sealed class Action {
    sealed class ImageAction() : Action()
    object AddImageAction: ImageAction() {
        var pos =  0
        var file: AlbumItemFile? = null
    }
    object RemoveImageAction: ImageAction() {
        var pos =  0   // 在album adapter pos
        var file: AlbumItemFile? = null
    }
    object AddNewPhotoAction: ImageAction() {
        var uri: Uri? = null
    }
    object RemoveNewPhotoAction: ImageAction() {
        var photoPos =  0    //如果从linkedhashmap中抽出照片的话，在照片中的 中的pos ,序列号 -1 -2 -3 -4 为了避开image
        var uri: Uri? = null
    }

    sealed class TextAction(): Action()
    object UpdateTextAction: TextAction() {
        var text:String? =null
    }
}

fun MutableLiveData<PostStatus>.reduce(action: Action) {
    val state = this.value!!

    state.updatingImages = action is Action.ImageAction
    state.albumAdapterPos = if ((action is Action.AddImageAction) or (action is Action.RemoveImageAction)) -1 else 0
    when (action) {
        is Action.AddImageAction -> {
            state.linkedHashMap[action.pos] = action.file!!
            state.albumAdapterPos = action.pos
            state.uploadAdapterPos = state.linkedHashMap.size - 1
        }
        is Action.RemoveImageAction -> {
            state.linkedHashMap.remove(action.pos)
            state.albumAdapterPos = -action.pos
        } is Action.AddNewPhotoAction -> {
            state.newPhotos ++
            state.linkedHashMap[-state.newPhotos] = action.uri
        } is Action.RemoveNewPhotoAction -> {
            state.newPhotos--
            val itr = state.linkedHashMap.iterator()
            val pairs = mutableListOf<Pair<Int, Any?>>()
            while (itr.hasNext()) {
                val pair = itr.next()
                if (pair.key == action.photoPos) {
                    itr.remove()
                    break
                }
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
