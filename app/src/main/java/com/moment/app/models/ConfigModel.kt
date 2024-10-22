package com.moment.app.models

import com.moment.app.utils.BaseBean

object ConfigModel {
    var momentConfig: MomentConfig? = null

    fun updateConfig() {

    }
}

class MomentConfig: BaseBean() {
    var enableFacebookTokenCheck: Boolean = false
    var disableHXLogin: Boolean = false
}