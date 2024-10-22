package com.moment.app.permissions

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.blankj.utilcode.util.PermissionUtils
import com.moment.app.R
import com.moment.app.databinding.MomentActionDialogBinding
import com.moment.app.utils.ActivityHolder
import com.moment.app.utils.BaseDialogFragment
import com.moment.app.utils.DialogUtils
import com.moment.app.utils.SPUtil
import java.util.Arrays

object PermissionHelper {
    /**
     * 权限请求成功
     */
    const val PERMISSION_REQUEST_SUCCESS: Int = 0

    /**
     * 权限请求失败
     */
    const val PERMISSION_REQUEST_FAIL: Int = 1

    const val CHECK_KEY_PREFIX: String = "lit_permission_check_"

    fun check(context: Context, scene: String, permissions: Array<String>, callback: Callback) {
        val permissionsList: MutableList<String?> = ArrayList(Arrays.asList(*permissions))

        var content = ""
        var failContent = ""

        if (permissionsList.contains(Manifest.permission.POST_NOTIFICATIONS)) {
            content = context.getString(R.string.enable_push_content_he)
            failContent = ""
        } else if (permissionsList.contains(Manifest.permission.CAMERA) && permissionsList.contains(
                Manifest.permission.RECORD_AUDIO
            )
        ) {
            content = context.getString(R.string.request_video_permission, scene)
            failContent = context.getString(R.string.request_video_permission_fail)
        } else if (permissionsList.contains(Manifest.permission.CAMERA)) {
            content = context.getString(R.string.request_photo_permission)
            failContent = context.getString(R.string.request_photo_permission_fail)
        } else if (permissionsList.contains(Manifest.permission.RECORD_AUDIO)) {
            content = context.getString(R.string.request_audio_permission, scene)
            failContent = context.getString(R.string.request_audio_permission_fail)
        } else if (permissionsList.contains(Manifest.permission.READ_EXTERNAL_STORAGE)
            || permissionsList.contains(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        ) {
            content = context.getString(R.string.request_save_permission, scene)
            failContent = context.getString(R.string.request_save_permission_fail)
        } else if (permissionsList.contains(Manifest.permission.ACCESS_FINE_LOCATION)
            || permissionsList.contains(Manifest.permission.ACCESS_COARSE_LOCATION)
        ) {
            content = context.getString(R.string.map_get_location_permission)
            failContent = context.getString(R.string.map_get_location_permission_process)
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (permissionsList.contains(Manifest.permission.READ_MEDIA_IMAGES)
                    || permissionsList.contains(Manifest.permission.READ_MEDIA_VIDEO)
                    || permissionsList.contains(Manifest.permission.READ_MEDIA_AUDIO)
                ) {
                    content = context.getString(R.string.request_save_permission, scene)
                    failContent = context.getString(R.string.request_save_permission_fail)
                }
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (permissionsList.contains(Manifest.permission.READ_EXTERNAL_STORAGE) || permissionsList.contains(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            ) {
                permissionsList.remove(Manifest.permission.READ_EXTERNAL_STORAGE)
                permissionsList.remove(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                permissionsList.add(Manifest.permission.READ_MEDIA_IMAGES)
                permissionsList.add(Manifest.permission.READ_MEDIA_VIDEO)
            }
        }

        var hasAll = true
        for (permission in permissionsList) {
            if (ContextCompat.checkSelfPermission(context, permission!!)
                != PackageManager.PERMISSION_GRANTED
            ) {
                hasAll = false
                break
            }
        }
        if (hasAll) {
            callback?.result(PERMISSION_REQUEST_SUCCESS)
            return
        }

        val finalFailContent = failContent

        if (permissionsList.contains(Manifest.permission.POST_NOTIFICATIONS)) {
            //不弹窗提示，上层已提示过
            request(context, permissionsList, finalFailContent, callback)
            return
        }

        MomentActionDialog.showBundle(context, object : SetupBundle{
            override fun setBundle(bundle: Bundle): Bundle {
                //bundle.putString()
                return bundle
            }

            override fun cancel() {
                callback?.result(PERMISSION_REQUEST_FAIL)
            }

            override fun confirm() {
                request(context, permissionsList, finalFailContent, callback)
            }
        })

    }

    private fun request(
        context: Context,
        permissionsList: List<String?>,
        finalFailContent: String,
        callback: Callback?
    ) {
        val activity: Activity? =  ActivityHolder.getCurrentActivity()
        if (activity == null) {
            callback!!.result(-1)
            return
        }
        val checkedBefore: Boolean = SPUtil.getBoolean(CHECK_KEY_PREFIX + permissionsList[0], false)

        if (checkedBefore && permissionsList.contains(Manifest.permission.POST_NOTIFICATIONS)) {
            var shouldShowRequestPermissionRationale = false

            for (permission in permissionsList) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    shouldShowRequestPermissionRationale = (shouldShowRequestPermissionRationale
                            || activity.shouldShowRequestPermissionRationale(permission!!))
                }
            }

            if (!shouldShowRequestPermissionRationale) {
                if (permissionsList.contains(Manifest.permission.POST_NOTIFICATIONS)) {
                    openNotificationSetting(activity)
                    return
                }
                //被拒绝了，并且不提醒
                PermissionUtils.launchAppDetailsSettings()
                return
            }
        }


        SPUtil.save(CHECK_KEY_PREFIX + permissionsList[0], true)
        RequestPermissionDialog.startRequest(
            activity,
            permissionsList.toTypedArray(),
            object : RequestPermissionDialog.RequestCallback {
                override fun onResult(result: Int) {
                    if (result != PERMISSION_REQUEST_SUCCESS) {
                        if (permissionsList.contains(Manifest.permission.POST_NOTIFICATIONS)) {
                            openNotificationSetting(activity)
                            return
                        }

                        MomentActionDialog.showBundle(context, object : SetupBundle{
                            override fun setBundle(bundle: Bundle): Bundle {
                                bundle.putString("content", finalFailContent)
                                return bundle
                            }

                            override fun cancel() {
                                callback?.result(result)
                            }

                            override fun confirm() {
                                if (permissionsList.contains(Manifest.permission.POST_NOTIFICATIONS)) {
                                     openNotificationSetting(activity)
                                } else {
                                    //被拒绝了，并且不提醒
                                    PermissionUtils.launchAppDetailsSettings()
                                }
                                callback?.result(result)
                            }
                        })
                    } else {
                        callback?.result(result)
                    }
                }
            })
    }

    interface Callback {
        /**
         * 权限请求回调
         * @param res 0-请求成功 [PermissionHelper.PERMISSION_REQUEST_SUCCESS]
         * 1-请求失败 [PermissionHelper.PERMISSION_REQUEST_FAIL]
         */
        fun result(res: Int)
    }
}

fun openNotificationSetting(context: Context) {
    try {
        val localIntent = Intent()
        localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        //直接跳转到应用通知设置的代码：
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            localIntent.setAction(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
            localIntent.putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
            context.startActivity(localIntent)
            return
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            localIntent.setAction("android.settings.APP_NOTIFICATION_SETTINGS")
            localIntent.putExtra("app_package", context.packageName)
            localIntent.putExtra("app_uid", context.applicationInfo.uid)
            context.startActivity(localIntent)
            return
        }
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
            localIntent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            localIntent.addCategory(Intent.CATEGORY_DEFAULT)
            localIntent.setData(Uri.parse("package:" + context.packageName))
            context.startActivity(localIntent)
            return
        }
        localIntent.setAction(Intent.ACTION_VIEW)
        localIntent.setClassName("com.android.settings", "com.android.setting.InstalledAppDetails")
        localIntent.putExtra("com.android.settings.ApplicationPkgName", context.packageName)
        context.startActivity(localIntent)
    } catch (e: Exception) {
        e.printStackTrace()
        println(" cxx   pushPermission 有问题")
    }
}

interface SetupBundle {
    fun setBundle(bundle: Bundle): Bundle
    fun cancel()
    fun confirm()
}

class MomentActionDialog: BaseDialogFragment() {

    companion object {
        fun showBundle(context: Context?, setupBundle: SetupBundle) {
            context?.let {
                DialogUtils.show(it, MomentActionDialog().apply {
                    arguments = setupBundle.setBundle(Bundle())
                    this.setupBundle = setupBundle
                })
            }
        }
    }

    var setupBundle: SetupBundle? = null
    private lateinit var binding: MomentActionDialogBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = MomentActionDialogBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isCancelable = false
        arguments?.getString("content")?.let {
            binding.title.text = it
        }
        binding.left.setBackgroundResource(R.drawable.gender_slector_drawable)
        binding.left.isSelected = false
        binding.right.setBackgroundResource(R.drawable.gender_slector_drawable)
        binding.right.isSelected = true

        binding.left.setOnClickListener {
            setupBundle?.cancel()
            this.dismiss()
        }
        binding.right.setOnClickListener {
            setupBundle?.confirm()
            this.dismiss()
        }
    }
}
