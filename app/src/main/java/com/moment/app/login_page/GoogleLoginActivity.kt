package com.moment.app.login_page

import android.os.Bundle
import com.didi.drouter.annotation.Router
import com.moment.app.utils.BaseActivity
import dagger.hilt.android.AndroidEntryPoint


@Router(scheme = ".*", host = ".*", path = "/login/google")
@AndroidEntryPoint
class GoogleLoginActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
}