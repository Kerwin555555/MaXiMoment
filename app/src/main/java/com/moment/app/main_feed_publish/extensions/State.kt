package com.moment.app.main_feed_publish.extensions

import com.moment.app.utils.BaseBean

class State : BaseBean(){

    // -1 , 34,23, -2, 4,53,5  负数为照片 正数为相册
    var linkedHashMap: LinkedHashMap<Int, Any?> = LinkedHashMap<Int, Any?>()  // value 是 URI 或者 mediafile
    var updatingImages: Boolean = false
    var albumAdapterPos: Int = 0   // > 0 为添加新的pos < 0删
    var newPhotos: Int = 0
    var uploadAdapterPos: Int = 0

    var editTextText: String? = null
}