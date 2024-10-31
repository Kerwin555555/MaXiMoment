package com.moment.app.main_feed_publish

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.moment.app.images.bean.MediaFile
import com.moment.app.main_feed_publish.extensions.Action
import com.moment.app.main_feed_publish.extensions.State
import com.moment.app.main_feed_publish.extensions.reduce

class PublishViewModel: ViewModel() {
    val publishLiveData = MutableLiveData<State>()
    init {
        publishLiveData.value = State()
    }


    fun dispatchAction(action: Action) {
        publishLiveData.reduce(action)
    }

    fun getImages() : LinkedHashMap<Int, Any?>{
        return publishLiveData.value!!.linkedHashMap
    }

    fun isImagesFull(): Boolean {
        return publishLiveData.value!!.linkedHashMap.size >= 9
    }

}