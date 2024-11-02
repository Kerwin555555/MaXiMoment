package com.moment.app.utils

import android.app.ActivityManager
import android.content.Context
import android.os.Process
import android.text.TextUtils
import android.util.Log
import com.blankj.utilcode.util.FileUtils
import com.moment.app.MomentApp
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStreamReader

object ProcessHelper {
    private var processName: String? = null

    fun getProcessName(pid: Int): String {
        try {
            val cmdlinePath = "/proc/$pid/cmdline"
            if (FileUtils.isFileExists(cmdlinePath)) {
                val content: StringBuilder? = readFile(cmdlinePath, "UTF-8")
                if (content != null) {
                    return content.toString().split("\u0000".toRegex())
                        .dropLastWhile { it.isEmpty() }
                        .toTypedArray()[0]
                }
            }
        } catch (var3: Exception) {
            Log.e("ProcessUtil", Log.getStackTraceString(var3))
        }

        return "unknown"
    }

    fun readFile(filePath: String?, charsetName: String?): java.lang.StringBuilder? {
        val file = File(filePath)
        val fileContent = java.lang.StringBuilder("")
        if (file == null || !file.isFile) {
            return null
        }

        var reader: BufferedReader? = null
        try {
            val `is` = InputStreamReader(FileInputStream(file), charsetName)
            reader = BufferedReader(`is`)
            var line: String? = null
            while ((reader.readLine().also { line = it }) != null) {
                if (fileContent.toString() != "") {
                    fileContent.append("\r\n")
                }
                fileContent.append(line)
            }
            reader.close()
            return fileContent
        } catch (e: IOException) {
            throw RuntimeException("IOException occurred. ", e)
        } finally {
            if (reader != null) {
                try {
                    reader.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }


    fun myProcessName(): String? {
        if (processName == null) {
            val var0: Class<*> = ProcessHelper::class.java
            synchronized(ProcessHelper::class.java) {
                if (processName == null) {
                    processName = getProcessName(MomentApp.appContext)
                }
            }
        }

        return processName
    }

    fun isMainProcess(c: Context?): Boolean {
        return TextUtils.equals(c?.packageName ?: "", myProcessName())
    }

    fun myShortProcessName(context: Context): String? {
        val proc = myProcessName()
        val result: String?
        if (context.packageName == proc) {
            result = "main"
        } else if (!TextUtils.isEmpty(proc)) {
            val index = proc!!.lastIndexOf(":")
            result = if (index != -1 && index + 1 < proc.length) {
                proc.substring(index + 1)
            } else {
                proc
            }
        } else {
            result = "unknown"
        }

        return result
    }

    fun getProcessName(context: Context): String? {
        var count = 0
        do {
            val processName = getProcessNameImpl(context)
            if (!TextUtils.isEmpty(processName)) {
                return processName
            }
        } while (count++ < 3)

        return null
    }

    private fun getProcessNameImpl(context: Context): String? {
        // get by ams
        val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val processes = manager.runningAppProcesses
        if (processes != null) {
            val pid = Process.myPid()
            for (processInfo in processes) {
                if (processInfo.pid == pid && !TextUtils.isEmpty(processInfo.processName)) {
                    return processInfo.processName
                }
            }
        }

        // get from kernel
        val ret = getProcessName(Process.myPid())
        if (!TextUtils.isEmpty(ret) && ret.contains(context.packageName)) {
            return ret
        }

        return null
    }
}