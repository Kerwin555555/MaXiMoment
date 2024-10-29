package com.moment.app.datamodel

import com.moment.app.utils.BaseBean

open class CommentItem : BaseBean() {
    /**
     * comment_id : 5c8378943fff2229a7a62cf9
     * content : I love him so much!
     * inner_comments : [{"comment_id":"5c837e363fff2236ed3b5256","content":"I love him so much!","content_user_id":"5c80cdf13fff221a850d696d","time_info":{"time":1552121398,"time_desc":"5d ago"},"user_info":{"avatar":"c05f00c6-40a7-11e9-b1c7-00163e02deb4","birthdate":"2000-01-01","gender":"girl","huanxin_id":"love123644180112479","nickname":"test-nick1","user_id":"5c80cdf13fff221a850d696b"}},{"comment_id":"5c837c783fff222cd404e24c","content":"I love him so much!","content_user_id":"5c80cdf23fff221a850d6970","time_info":{"time":1552120952,"time_desc":"5d ago"},"user_info":{"avatar":"c05f00c6-40a7-11e9-b1c7-00163e02deb4","birthdate":"2000-01-01","gender":"girl","huanxin_id":"love123644180132053","nickname":"test-nick3","user_id":"5c80cdf13fff221a850d696d"}}]
     * time_info : {"time":1552119956,"time_desc":"5d ago"}
     * user_info : {"avatar":"c05f00c6-40a7-11e9-b1c7-00163e02deb4","birthdate":"2000-01-01","gender":"girl","huanxin_id":"love123644180231911","nickname":"test-nick6","user_id":"5c80cdf23fff221a850d6970"}
     */
    var comment_id: String? = null
    var content: String? = null
    var time_info: TimeInfoBean? = null
    var hasImpressionTrack: Boolean = false //曝光埋点标志
    var user_info: UserInfo? = null
    var inner_comments: List<InnerCommentsBean>? = null
    var show_outside: List<InnerCommentsBean>? = ArrayList()
    var hasClickedSeeMore: Boolean = false // 本地数据
    var show_pos: Int = 0
    var is_fold: Boolean = false
    var isFakeCommentId: Boolean = false
    var loadingStatus: Int = 1 // 本地数据 0 isLoading -> 1 load success无需显示 -> 2 LoadFailed
    var comment_like_num: Int = 0
    var comment_liked: Boolean = false

    fun showSeeMore(): Boolean {
        return canShowViewMore() && !hasClickedSeeMore
    }

    fun canShowViewMore(): Boolean {
        if (!is_fold) {
            return false
        }
        return if (showOutSideHasValue() && innerCommentsHasValue()) {
            if (show_outside!!.size > inner_comments!!.size) {
                //后台 bug
                false
            } else if (show_outside!!.size == inner_comments!!.size) {
                false
            } else {
                true
            }
        } else if (showOutSideHasValue()) {
            //不可能发生
            false
        } else if (innerCommentsHasValue()) {
            true
        } else {
            false
        }
    }

    fun showOutSideHasValue(): Boolean {
        return show_outside != null && show_outside!!.size > 0
    }

    fun innerCommentsHasValue(): Boolean {
        return inner_comments != null && inner_comments!!.size > 0
    }

    class TimeInfoBean : BaseBean() {
        /**
         * time : 1552119956
         * time_desc : 5d ago
         */
        var time: Int = 0
        var time_desc: String? = null
    }

    class InnerCommentsBean : CommentItem() {
        var content_user_id: String? = null
    }
}

class CommentsList : BaseBean() {
    var comments: MutableList<CommentItem>? = null
    var cursor: Int = 0
}

