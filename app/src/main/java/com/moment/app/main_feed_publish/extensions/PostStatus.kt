package com.moment.app.main_feed_publish.extensions

import com.moment.app.utils.BaseBean

class PostStatus : BaseBean(){

    // -1 , 34,23, -2, 4,53,5  负数为照片 正数为相册
    var linkedHashMap: LinkedHashMap<Int, Any?> = LinkedHashMap<Int, Any?>()  // value 是 URI 或者 mediafile
    @Transient
    var newPhotos: Int = 0
    @Transient
    var latestImageAction: Action.ImageAction? = null

    var editTextText: String? = ""


    fun isTextOk() : Boolean{     //计算非空个数
        if (editTextText.isNullOrEmpty()) {
            return false
        }
        var total = 0
        for (ch in editTextText!!.trim { it <= ' ' }.toCharArray()) {
            if (ch != ' ' && ch != '\n' && ch != '\r') {
                total++
            }
        }
        return total != 0
    }

    fun  capableOfBeingDispatched(): Boolean {
        return isTextOk() || linkedHashMap.size != 0
    }
}