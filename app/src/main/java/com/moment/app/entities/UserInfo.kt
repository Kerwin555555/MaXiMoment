package com.moment.app.entities

import com.moment.app.utils.BaseBean

data class UserInfo(
    var userId: String? = "",
    var gender: String? = "",
    var age: Int? = 0,
    var name: String? = "MomentFan"
): BaseBean() {
}