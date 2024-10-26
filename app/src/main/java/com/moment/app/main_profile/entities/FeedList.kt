package com.moment.app.main_profile.entities

import com.moment.app.datamodel.UserInfo
import com.moment.app.utils.BaseBean

class FeedList : BaseBean() {
    /**
     * feeds : [{"comment_num":1,"content":"I love him so much!","create_time":{"time":1552118149,"time_desc":"6h ago"},"id":"5c8371853fff2219345b69ae","like_num":0,"pics":[],"user_id":"5c80cdf23fff221a850d6970"},{"comment_num":0,"content":"main3","create_time":{"time":1552035651,"time_desc":"1d ago"},"id":"5c822f433fff224380673a1b","like_num":0,"pics":[],"user_id":"5c80cdf23fff221a850d696e"}]
     * has_next : true
     * next_start : 2
     */
    var has_next: Boolean = false
    var next_start: Int = 0
    var feeds: List<PostBean>? = null


}

class PostBean: BaseBean() {
    var id: String? = null
    var pics_shape : MutableList<PicShape>? = null
    var user_id: String? = ""
    var user_info: UserInfo? = null
    var content: String? = null
    var create_time: CreateTimeBean? = null
    var comment_num: Int? = 0
    var like_num: Int? = 0
    var liked = false

    fun isPictureFeed(): Boolean {
        return pics_shape != null && !pics_shape!!.isEmpty()
    }
}

class CreateTimeBean : BaseBean() {
    /**
     * time : 1552118149
     * time_desc : 6h ago
     */
    var time: Long = 0
    var time_desc: String? = null
}

class PicShape(var fileKey: String, var width: Int? = null, var height: Int? = null) : BaseBean()
