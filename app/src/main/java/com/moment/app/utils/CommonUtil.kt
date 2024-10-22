package com.moment.app.utils

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.database.Cursor
import android.net.ConnectivityManager
import android.net.Uri
import android.provider.MediaStore
import android.telephony.TelephonyManager
import android.text.TextUtils
import android.util.Pair
import com.blankj.utilcode.util.LanguageUtils
import com.moment.app.MomentApp
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.Locale

object CommonUtil {
    fun getActivity(cont: Context?): Activity? {
        if (cont == null) return null
        else if (cont is Activity) return cont
        else if (cont is ContextWrapper) return getActivity(cont.baseContext)

        return null
    }

    fun getCountryCode(): Pair<Int, String> {
        val manager = MomentApp.appContext
            .getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        if (manager?.simCountryIso != null) {
            val code = manager.simCountryIso.uppercase(Locale.getDefault())
            if (!TextUtils.isEmpty(code)) {
                return Pair<Int, String>(0, code)
            }
        }
        return Pair(1, getLanguageLocale().country)
    }

    fun getLanguageLocale(): Locale {
        val apply = LanguageUtils.getAppliedLanguage() ?: return getSysLocale()
        return apply
    }

    //以上获取方式需要特殊处理一下
    fun getSysLocale(): Locale {
        return LanguageUtils.getSystemLanguage()
    }


    fun getRealPathFromUri(context: Context, contentUri: Uri): String? {
        var cursor: Cursor? = null
        try {
            val proj = arrayOf(MediaStore.Images.Media.DATA)
            cursor = context.contentResolver.query(contentUri, proj, null, null, null)
            val column_index = cursor!!.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            cursor.moveToFirst()
            return cursor.getString(column_index)
        } catch (e: Exception) {
            return contentUri.path
        } finally {
            cursor?.close()
        }
    }

    fun isWifiConnected(context: Context?): Boolean {
        if (context != null) {
            val mConnectivityManager = context
                .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            if (mConnectivityManager == null) {
                return false
            }
            val mWiFiNetworkInfo = mConnectivityManager
                .getNetworkInfo(ConnectivityManager.TYPE_WIFI)
            if (mWiFiNetworkInfo != null) {
                return mWiFiNetworkInfo.isAvailable
            }
        }
        return false
    }

    fun calculateMD5(context: Context, fileUri: Uri?): String? {
        val digest: MessageDigest
        try {
            digest = MessageDigest.getInstance("MD5")
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
            return null
        }

        val `is`: InputStream?
        try {
            `is` = context.applicationContext.contentResolver.openInputStream(fileUri!!)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
            return null
        }

        val buffer = ByteArray(8192)
        var read: Int
        try {
            while ((`is`!!.read(buffer).also { read = it }) > 0) {
                digest.update(buffer, 0, read)
            }
            val messageDigest = digest.digest()
            // Create Hex String
            val hexString = StringBuilder()
            for (aMessageDigest in messageDigest) {
                var h = Integer.toHexString(0xFF and aMessageDigest.toInt())
                while (h.length < 2) h = "0$h"
                hexString.append(h)
            }
            return hexString.toString()
        } catch (e: IOException) {
            throw RuntimeException("Unable to process file for MD5", e)
        } finally {
            try {
                `is`!!.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    fun md5(s: String): String {
        val MD5 = "MD5"
        try {
            // Create MD5 Hash
            val digest = MessageDigest
                .getInstance(MD5)
            digest.update(s.toByteArray())
            val messageDigest = digest.digest()

            // Create Hex String
            val hexString = StringBuilder()
            for (aMessageDigest in messageDigest) {
                var h = Integer.toHexString(0xFF and aMessageDigest.toInt())
                while (h.length < 2) h = "0$h"
                hexString.append(h)
            }
            return hexString.toString()
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        }
        return s
    }
}