package com.moment.app.utils

import android.annotation.SuppressLint
import android.content.Context
import android.text.TextUtils
import com.blankj.utilcode.util.DeviceUtils
import com.blankj.utilcode.util.PhoneUtils
import com.tencent.mmkv.MMKV
import java.util.UUID

object MachineUUIDManager {
    val PREFS_FILE: String = "sp_moment_device_id"
    val PREFS_DEVICE_ID: String = "device_id"
    var uuid: String? = null

    fun MachineUUIDManager(context: Context?): String? {
        if (uuid == null) {
            synchronized(MachineUUIDManager::class.java) {
                if (uuid == null) {
                    val prefs = MMKV.defaultMMKV()
                    val id = prefs.getString("device_id", null as String?)
                    if (id != null) {
                        uuid = id
                    } else {
                        try {
                            val androidID = DeviceUtils.getAndroidID()
                            DeviceUtils.getUniqueDeviceId()
                            uuid = if (!TextUtils.isEmpty(androidID)) { androidID
                            } else {
                                @SuppressLint("MissingPermission") val deviceId =
                                    PhoneUtils.getDeviceId()
                                 deviceId ?: UUID.randomUUID().toString()
                            }
                        } catch (exception: Exception) {
                            throw RuntimeException(exception)
                        }

                        prefs.edit().putString("device_id", uuid.toString()).apply()
                    }
                }
            }
        }
        return uuid
    }

    fun getDeviceUuid(): String? {
        return uuid
    }
}