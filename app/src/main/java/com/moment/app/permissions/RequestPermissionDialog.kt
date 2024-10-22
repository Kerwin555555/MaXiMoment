package com.moment.app.permissions

import android.content.Context
import android.os.Bundle
import android.view.View
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.LogUtils
import com.moment.app.utils.BaseActivity
import com.moment.app.utils.DialogUtils
import com.moment.app.utils.ProgressDialog

class RequestPermissionDialog : ProgressDialog() {
    private var requestCallback: RequestCallback? = null

    fun setRequestCallback(requestCallback: RequestCallback?) {
        this.requestCallback = requestCallback
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        LogUtils.d("RequestPermissionDialog", "show")
        this.requestPermissions(this.requireArguments().getStringArray("data")!!, 9009)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 9009) {
            if (grantResults.size <= 0) {
                if (this.requestCallback != null) {
                    requestCallback!!.onResult(-1)
                }
            } else {
                var hasAll = true
                val var5 = grantResults
                val var6 = grantResults.size

                for (var7 in 0 until var6) {
                    val res = var5[var7]
                    if (res != 0) {
                        hasAll = false
                        break
                    }
                }

                if (this.requestCallback != null) {
                    requestCallback!!.onResult(if (hasAll) 0 else 1)
                }
            }

            this.dismiss()
        }
    }

    interface RequestCallback {
        fun onResult(var1: Int)
    }

    companion object {
        fun startRequest(
            context: Context?,
            permissions: Array<String?>?,
            callback: RequestCallback?
        ) {
            val dialog = RequestPermissionDialog()
            dialog.setRequestCallback(callback)
            val bundle = Bundle()
            bundle.putStringArray("data", permissions)
            dialog.setArguments(bundle)
            val activity = ActivityUtils.getTopActivity()
            if (activity is BaseActivity) {
                DialogUtils.show(activity, dialog)
            }
        }
    }
}
