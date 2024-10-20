package com.moment.app.datamodel

import com.moment.app.utils.BaseBean

data class UserInfo(
    var userId: String? = "",
    var gender: String? = "",
    var age: Int? = 0,
    var name: String? = "MomentFan",
    var session: String? = "",
    var finished_info: Boolean = true,
    var country: String? = "",
    var forbidden_session: String? ="",
    var huanxin:HuanxinBean? = null
): BaseBean() {
}

class HuanxinBean : BaseBean() {
    /**
     * password : b7702c283c823038ce4776cf9a3735fb
     * user_id : love1236383185000000731
     */
    var password: String? = null
    var user_id: String? = null
}