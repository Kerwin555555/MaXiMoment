package com.moment.app.router

import android.util.Log
import com.didi.drouter.annotation.Interceptor
import com.didi.drouter.router.IRouterInterceptor
import com.didi.drouter.router.Request
import com.moment.app.models.LoginModel
import com.moment.app.utils.MOMENT_APP


@Interceptor(priority = 2, global = true)
class UserDetailInterceptor : IRouterInterceptor {
    override fun handle(request: Request) {
        if (request.uri.path == "/user") {
            var id = RouteParams.getString(request, "id")
            val info = request.getSerializable("info")
            if (LoginModel.isMe(id)) {
                request.interceptor.onInterrupt()
                Log.d(MOMENT_APP, "hi intercept")
            } else {
                Log.d(MOMENT_APP, "hi contionue")
                request.interceptor.onContinue()
            }
            return
        }

        request.interceptor.onContinue()
    }
}