package com.moment.app.main_home.subfragments.entities

import com.moment.app.entities.UserInfo
import com.moment.app.utils.BaseBean

data class UserInfoList(
    var has_next: Boolean = false,
    var next_start: Int = 0,
    var user_infos: MutableList<UserInfo>? = null
): BaseBean() {

}