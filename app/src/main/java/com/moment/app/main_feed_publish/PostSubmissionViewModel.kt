package com.moment.app.main_feed_publish

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.moment.app.main_feed_publish.extensions.Action
import com.moment.app.main_feed_publish.extensions.PostStatus
import com.moment.app.main_feed_publish.extensions.reduce

class PostSubmissionViewModel: ViewModel() {
    val publishLiveData = MutableLiveData<PostStatus>()
    init {
        publishLiveData.value = PostStatus()
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