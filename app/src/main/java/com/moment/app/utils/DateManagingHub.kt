package com.moment.app.utils

import android.content.Context
import android.view.View
import androidx.core.content.ContextCompat
import com.bigkoo.pickerview.builder.TimePickerBuilder
import com.bigkoo.pickerview.listener.OnTimeSelectChangeListener
import com.bigkoo.pickerview.listener.OnTimeSelectListener
import com.bigkoo.pickerview.view.TimePickerView
import com.moment.app.BuildConfig
import com.moment.app.R
import com.moment.app.utils.sntp.SntpClock
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.max

object DateManagingHub {
    val china: TimeZone = TimeZone.getTimeZone("GMT+08:00")

    private val format: DateFormat = SimpleDateFormat("yyyy-MM-dd")


    val startOfDay: Date
        get() = getStartOfDay(Date(SntpClock.currentTimeMillis()))

    val endOfDay: Date
        get() = getEndOfDay(Date(SntpClock.currentTimeMillis()))

    fun getInterval(start: Long, end: Long): Int {
        if (start > end) {
            return 0
        }
        return ((end - start) / (24 * 60 * 60 * 1000)).toInt()
    }

    fun getDateByDiffYear(dYear: Int): Date {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = SntpClock.currentTimeMillis()
        calendar.add(Calendar.YEAR, dYear)
        return calendar.time
    }

    fun getStartOfDay(date: Date): Date {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = date.time
        calendar[Calendar.HOUR_OF_DAY] = 0
        calendar[Calendar.MINUTE] = 0
        calendar[Calendar.SECOND] = 0
        calendar[Calendar.MILLISECOND] = 0
        return calendar.time
    }

    fun getEndOfDay(date: Date): Date {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = date.time
        calendar[Calendar.HOUR_OF_DAY] = 23
        calendar[Calendar.MINUTE] = 59
        calendar[Calendar.SECOND] = 59
        calendar[Calendar.MILLISECOND] = 999
        return calendar.time
    }

    fun getAge(birthday: String?): Int {
        try {
            val date = format.parse(birthday)
            val age = getDiffYears(date, startOfDay)
            return max(age.toDouble(), 0.0).toInt()
        } catch (e: Exception) {
            if (BuildConfig.DEBUG) {
                e.printStackTrace()
            }
            return 0
        }
    }

    fun getDiffYears(first: Date?, last: Date?): Int {
        val a = getCalendar(first)
        val b = getCalendar(last)
        var diff = b[Calendar.YEAR] - a[Calendar.YEAR]
        if (a[Calendar.MONTH] > b[Calendar.MONTH] ||
            (a[Calendar.MONTH] == b[Calendar.MONTH] && a[Calendar.DATE] > b[Calendar.DATE])
        ) {
            diff--
        }
        return diff
    }

    fun getCalendar(date: Date?): Calendar {
        val cal = Calendar.getInstance(Locale.getDefault())
        cal.time = date
        return cal
    }

    private fun getCalendar(diffYear: Int): Calendar {
        val now = Calendar.getInstance()
        now.timeInMillis = SntpClock.currentTimeMillis()
        now.add(Calendar.YEAR, diffYear)
        return now
    }

    fun chooseDate(
        context: Context?,
        defaultDate: Calendar,
        listener: OnTimeSelectListener?,
        cancelListener: View.OnClickListener?,
        selectChangeListener: OnTimeSelectChangeListener?
    ): TimePickerView {
        val ageMax: Int = 60
        val ageMin: Int = 18

        var startDate = getCalendar(-ageMax)
        val endDate = getCalendar(-ageMin)

        if (startDate.after(endDate)) {
            startDate = endDate
        }


        return TimePickerBuilder(context, listener)
            .setType(booleanArrayOf(true, true, true, false, false, false)) // type of date
            .setOutSideCancelable(true) // default is true
            .isCyclic(false) // default is false
            .setBgColor(0xffffffff.toInt())
            .setTextColorCenter(ContextCompat.getColor(context!!, R.color.moment_text_ff1d1d1d))
            .setOutSideColor(-0x56000000)
            .addOnCancelClickListener(cancelListener)
            .setRangDate(startDate, endDate)
            .setDate(defaultDate)
            .setTimeSelectChangeListener(selectChangeListener)
            .setLabel("", "", "", "hours", "mins", "seconds")
            .build()
    }

    fun birthdayToDate(dateString: String) : Date {

        // 使用 SimpleDateFormat 解析字符串为 Date 对象
        val parsedDate = format.parse(dateString)
        // 如果你一定要使用 Calendar，你可以这样做：
        val calendar = Calendar.getInstance()
        calendar.time = parsedDate
        return parsedDate!!
    }
}
