import android.os.Build
import android.text.TextUtils
import android.util.Base64
import com.blankj.utilcode.util.NetworkUtils
import com.moment.app.network.MomentKeysManager
import com.moment.app.utils.MomentCoreParams
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import org.json.JSONException
import org.json.JSONObject
import java.security.spec.AlgorithmParameterSpec
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

//package com.moment.app.network
//
//import android.os.Build
//import android.text.TextUtils
//import android.util.Base64
//import com.blankj.utilcode.util.NetworkUtils
//import okhttp3.HttpUrl
//import okhttp3.Interceptor
//import okhttp3.Request
//import okhttp3.Response
//import org.json.JSONException
//import org.json.JSONObject
//import java.io.IOException
//import java.security.spec.AlgorithmParameterSpec
//import javax.crypto.Cipher
//import javax.crypto.spec.IvParameterSpec
//import javax.crypto.spec.SecretKeySpec
//import kotlin.reflect.KParameter
//
class HttpConfigInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
         val request = chain.request()
         val builder = request.newBuilder()

         val queryBuilder = request.url.newBuilder()
         val session = MomentKeysManager.getSessionOrForbid()
         queryBuilder.addQueryParameter("sid", session)
             .addQueryParameter("loc", "locinfo.countryid")
             .addQueryParameter("uuid", "appinfo.uuid")
             .addQueryParameter("version", "AppInfo.versionName")
             .addQueryParameter("lang", "AppInfo.lang")
             .addQueryParameter("os", "${Build.VERSION.SDK_INT}")

         val url = queryBuilder.build()
         builder.url(url)
        try {
            builder.addHeader("v-mode", (if (NetworkUtils.isUsingVPN()) 1 else 0).toString())
        } catch (e: java.lang.Exception) {
            //ignore
        }

        val encodePath = request.url.encodedPath
        if (encodePath.contains("moment/get_sms_code")
            || encodePath.contains("moment/user/phone_login")
            || encodePath.contains("moment/user/google_login")
            || encodePath.contains("moment/user/facebook_login")
            || encodePath.contains("moment/user/info")
        ) {
            guardNet(builder)
        }

         return chain.proceed(builder.build())
    }

    private fun guardNet(builder: Request.Builder) {
        val `object` = JSONObject()
        try {
            `object`.put("uuid", "AppInfo.uuid")
        } catch (e: JSONException) {
            e.printStackTrace()
            return
        }
        val fingerprint: String = getEncryptString(`object`.toString(), MomentCoreParams.KEY_UUID)!!
        if (!TextUtils.isEmpty(fingerprint)) {
            builder.addHeader("fingerprint", fingerprint)
        }
    }


    fun getEncryptString(str: String?, key: String?): String? {
        try {
            val result: ByteArray = encryptAndBase64Encode(
                str!!.toByteArray(),
                key!!.toByteArray(),
                "abcdef1234567890".toByteArray(),
                "AES/CBC/PKCS5Padding"
            )!!
            if (result != null) {
                return String(result)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }


    /**
     * AES encrypt then base64 decode
     *
     * @param data           Data to encrypt
     * @param key            Encrypt key
     * @param iv             Encrypt key
     * @param transformation AES/CBC/PKCS5Padding
     * @return Encrypted bytes
     * @throws Exception Encrypt exception
     */
    @Throws(java.lang.Exception::class)
    fun encryptAndBase64Encode(
        data: ByteArray?,
        key: ByteArray?,
        iv: ByteArray?,
        transformation: String?
    ): ByteArray? {
        if (data == null || data.size == 0 || key == null || key.size == 0 || iv == null || iv.size == 0 ||
            transformation == null || transformation.length == 0) {
            return null
        }

        val ivSpec: AlgorithmParameterSpec = IvParameterSpec(iv)
        val newKey = SecretKeySpec(key, "AES")
        val cipher = Cipher.getInstance(transformation)
        cipher.init(Cipher.ENCRYPT_MODE, newKey, ivSpec)
        return Base64.encode(cipher.doFinal(data), Base64.NO_WRAP or Base64.URL_SAFE)
    }
}