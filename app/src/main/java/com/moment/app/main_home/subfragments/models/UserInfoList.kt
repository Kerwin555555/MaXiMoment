package com.moment.app.main_home.subfragments.models

import com.moment.app.datamodel.UserInfo
import com.moment.app.utils.BaseBean

data class UserInfoList(
    var has_next: Boolean = false,
    var next_start: Int = 0,
    var user_infos: MutableList<UserInfo>? = null
): BaseBean() {

}