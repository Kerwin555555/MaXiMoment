package com.moment.app.router

import android.util.Log
import com.didi.drouter.annotation.Interceptor
import com.didi.drouter.router.IRouterInterceptor
import com.didi.drouter.router.Request
import com.moment.app.models.UserLoginManager
import com.moment.app.utils.MOMENT_APP


@Interceptor(priority = 2, global = true)
class UserProfileInterceptor : IRouterInterceptor {
    override fun handle(request: Request) {
        if (request.uri.path == "/user") {
            var id = DRouterHelper.getString(request, "id")
            val info = request.getSerializable("info")
            if (UserLoginManager.isMe(id)) {
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