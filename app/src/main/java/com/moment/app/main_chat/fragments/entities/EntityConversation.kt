package com.moment.app.main_chat.fragments.entities

import androidx.annotation.NonNull
import androidx.room.Entity
import androidx.room.Ignore
import com.hyphenate.chat.EMConversation
import com.moment.app.datamodel.UserInfo
import com.moment.app.utils.BaseBean


@Entity(tableName = "user_conversation", primaryKeys = ["id", "userId"])
class EntityConversation(
    @NonNull val id: String,
    @NonNull val userId: String
) : BaseBean() {
    // 其他字段和方法
    var createTime: String? = null
    var updateTime: Long = 0
    var pinned: Int = 0
    var type: String? = null
    var status: Int = 0
    var userInfo: UserInfo? = null

    var conversationType: Int = 0
    var draft: String? = null // 草稿
    var remindEndTime: Long = 0// 置顶结束时间
    var flag: Int = 0 // 标记为稍后回复

    @Ignore
    var emConversation: EMConversation? = null

    @Ignore
    var isAtMe: Boolean = false
}