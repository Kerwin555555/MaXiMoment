package com.moment.app.utils.sntp

import android.content.Context
import android.util.Log
import com.blankj.utilcode.util.LogUtils
import com.moment.app.R
import com.moment.app.utils.MOMENT_APP
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.selects.select
import java.lang.ref.WeakReference
import java.util.concurrent.CopyOnWriteArrayList


object SntpClock {
    /**
     * 根据日活动态调整服务器地址
     *
     * [全球NTP服务器](https://v6527g89pb.feishu.cn/docx/D7ZjdwsSwogQotxdxDJcqqgHnzb)
     *
     * 默认的NTP服务器地址配置，适用于LitMatch
     * "id.pool.ntp.org,ph.pool.ntp.org,vn.pool.ntp.org,th.pool.ntp.org,my.pool.ntp.org,br.pool.ntp.org,tr.pool.ntp.org,asia.pool.ntp.org,north-america.pool.ntp.org,pool.ntp.org"
     * 如需替换请在app的string.xml中配置ntp_server_list替换module中的配置
     * 每两个地址之间用逗号隔开
     */
    private var NTP_SERVERS = arrayOf<String>()

    private const val TAG = "SntpClock"
    private const val KEY_NTP_TIME_OFFSET = "time_offset_sp"
    private const val NTP_TIME_OUT_TIME = 3_000
    private var offset = 0L
    private var listenerList: CopyOnWriteArrayList<WeakReference<OnSyncTimeListener>> = CopyOnWriteArrayList()

    private val requestResult = HashMap<String, Long>().apply {
        NTP_SERVERS.forEach {
            this[it] = 0L
        }
    }

    fun init(context: Context) {
        NTP_SERVERS =  context.resources.getStringArray(R.array.ntp_server_list)
        offset = getTimeOffset(context)
        LogUtils.d(TAG, "init: servers = $NTP_SERVERS")
        LogUtils.d(TAG, "init: offset = $offset")
        syncTime(context)
    }

    private fun getTimeOffset(context: Context): Long {
        val sp = context.getSharedPreferences(KEY_NTP_TIME_OFFSET, Context.MODE_PRIVATE)
        return sp.getLong("time_offset", 0)
    }

    @JvmStatic
    fun currentTimeMillis(): Long {
        return System.currentTimeMillis() + offset
    }

    @JvmStatic
    fun currentTime(): Long {
        return currentTimeMillis() / 1000
    }

    /**
     * 同时请求NTP_SERVERS中所有服务器，当有一个请求成功时，立即返回结果
     */
    fun syncTime(context: Context) {
        GlobalScope.launch(Dispatchers.IO) {
            val jobs = List(NTP_SERVERS.size) { index ->
                async {
                    val sntpClient = SntpClient()
                    if (sntpClient.requestTime(NTP_SERVERS[index], NTP_TIME_OUT_TIME)) {
                        val offset = sntpClient.ntpTime - System.currentTimeMillis()
                        requestResult[NTP_SERVERS[index]] = offset
                        offset
                    } else {
                        0L
                    }
                }
            }
            val tempOffset :Long = select {
                jobs.forEach { job ->
                    job.onAwait {
                        job.cancel()
                        return@onAwait it
                    }
                }
            }
            if (tempOffset != 0L) {
                val sp = context.getSharedPreferences(KEY_NTP_TIME_OFFSET, Context.MODE_PRIVATE)
                sp.edit().putLong("time_offset", offset).apply()
                offset = tempOffset
            }
            notifyTimeSync()
        }

    }

    private fun notifyTimeSync() {
        listenerList.forEach {
            it.get()?.onSyncTime(currentTimeMillis())
        }
        LogUtils.d(TAG, "syncTime: offset = $offset currentTimeMillis = ${currentTimeMillis()}")
    }

    fun registerSyncTimeListener(listener: OnSyncTimeListener) {
        listenerList.add(WeakReference(listener))
    }

    fun unregisterSyncTimeListener(listener: OnSyncTimeListener) {
        listenerList.remove(WeakReference(listener))
    }


    interface OnSyncTimeListener {
        fun onSyncTime(currentTime: Long)
    }

}