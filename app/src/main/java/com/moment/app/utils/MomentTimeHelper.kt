import com.moment.app.utils.sntp.SntpClock
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date

object MomentTimeHelper {
    const val ONE_MINUTE: Int = 60
    const val ONE_HOUR: Int = 60 * 60
    const val ONE_DAY: Int = 24 * 60 * 60
    const val ONE_DAY_MILLLIS: Int = 24 * 60 * 60 * 1000
    const val ONE_WEEK: Int = 24 * 60 * 60 * 7
    var sdf: SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm")
    private val sdf2 = SimpleDateFormat("HH:mm")
    private val sdf3 = SimpleDateFormat("MM-dd HH:mm")
    private val sdf4 = SimpleDateFormat("MM/dd")
    private val sdf5 = SimpleDateFormat("yyyy/MM/dd")
    private val sdf6 = SimpleDateFormat("dd/MM/yyyy")
    private val sdf7 = SimpleDateFormat("yyyy/MM/dd HH:mm")
    private val sdf8 = SimpleDateFormat("yyyy-MM-dd")

    private var targetCalendar: Calendar? = null
    private var nowCalendar: Calendar? = null

    fun parseTimeCompat(unix: Long, pattern: String?): String {
        val df = SimpleDateFormat(pattern)
        //df.setTimeZone(TimeZone.getTimeZone("GMT"));
        val timestamp = Timestamp(unix)
        val date = df.format(timestamp)
        return date
    }

    fun parseTime(time: Long, pattern: String?): String {
        sdf.applyPattern(pattern)
        return sdf.format(time)
    }

    fun parseChatListTime(time: Long): String {
        // 今天凌晨时间
        val todayStartTime = getDayStartTime(SntpClock.currentTimeMillis())
        // 昨天凌晨时间
        val yesterdayStartTime = getDayStartTime(SntpClock.currentTimeMillis() - ONE_DAY_MILLLIS)
        // 7天前的凌晨时间
        val sevenDayStartTime = getDayStartTime(SntpClock.currentTimeMillis() - ONE_DAY_MILLLIS * 7)
        if (time > todayStartTime) { // 今天
            return sdf2.format(time)
        }
        if (time > yesterdayStartTime) { // 昨天
            return "Yesterday" + " " + sdf2.format(
                time
            )
        }
        if (time > sevenDayStartTime) { // 前天开始的前六个自然日
            return getWeek(time)
        }
        if (isSameYear(time)) { // 今年
            return sdf4.format(time)
        }
        return sdf5.format(time)
    }

    fun parseTimeWithFormat5(timeMills: Long): String {
        return sdf5.format(timeMills)
    }

    fun parseTimeWithFormat6(timeMills: Long): String {
        return sdf6.format(timeMills)
    }

    /**
     * 获取凌晨时间
     * @param time
     * @return
     */
    fun getDayStartTime(time: Long): Long {
        val date = Date(time)
        val today = sdf5.format(date.time)
        try {
            return sdf5.parse(today).time
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return time
    }

    /**
     * 获取星期
     * @param time
     * @return
     */
    private fun getWeek(time: Long): String {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = time
        val week = calendar[Calendar.DAY_OF_WEEK]
        return when (week) {
            Calendar.SUNDAY -> "sunday"
            Calendar.MONDAY -> "monday"
            Calendar.TUESDAY -> "tuesday"
            Calendar.WEDNESDAY -> "wednesday"
            Calendar.THURSDAY -> "thursday"
            Calendar.FRIDAY -> "friday"
            else -> "saturday"
        }
    }

    fun isSameYear(time: Long): Boolean {
        if (targetCalendar == null) {
            targetCalendar = Calendar.getInstance()
        }
        if (nowCalendar == null) {
            nowCalendar = Calendar.getInstance()
        }
        targetCalendar!!.timeInMillis = time
        nowCalendar!!.timeInMillis = SntpClock.currentTimeMillis()
        return targetCalendar!![Calendar.YEAR] == nowCalendar!![Calendar.YEAR]
    }

    fun parseChatTime(time: Long): String {
        val currentTime: Long = SntpClock.currentTimeMillis()

        val gap = getGapCount(Date(time), Date(currentTime))
        return if (gap < 1) {
            sdf2.format(Date(time))
        } else if (gap == 1) {
            "yesterday " + sdf2.format(Date(time))
        } else {
            sdf.format(Date(time))
        }
    }

    /**
     * 获取两个日期之间的间隔天数
     *
     * @return
     */
    fun getGapCount(startDate: Date?, endDate: Date?): Int {
        val fromCalendar = Calendar.getInstance()
        fromCalendar.time = startDate
        fromCalendar[Calendar.HOUR_OF_DAY] = 0
        fromCalendar[Calendar.MINUTE] = 0
        fromCalendar[Calendar.SECOND] = 0
        fromCalendar[Calendar.MILLISECOND] = 0

        val toCalendar = Calendar.getInstance()
        toCalendar.time = endDate
        toCalendar[Calendar.HOUR_OF_DAY] = 0
        toCalendar[Calendar.MINUTE] = 0
        toCalendar[Calendar.SECOND] = 0
        toCalendar[Calendar.MILLISECOND] = 0

        return ((toCalendar.time.time - fromCalendar.time.time) / (1000 * 60 * 60 * 24)).toInt()
    }

    fun getGapCount(startTime: Long, endTime: Long): Int {
        return ((endTime - startTime) / (1000 * 60 * 60 * 24)).toInt()
    }

    /**
     * @param time 单位为s
     * @return 00:00:00
     */
    fun parseTime(time: Long): String {
        if (time <= 0) {
            return "00:00:00"
        }
        val s = (time % 60).toInt()
        if (time < 60) {
            return String.format("00:00:%s", if (s > 9) s else "0$s")
        }
        val m = ((time % 3600) / 60).toInt()
        if (time < 3600) {
            return String.format("00:%s:%s", if (m > 9) m else "0$m", if (s > 9) s else "0$s")
        }
        val h = (time / 3600).toInt()
        return String.format(
            "%s:%s:%s",
            if (h > 9) h else "0$h",
            if (m > 9) m else "0$m",
            if (s > 9) s else "0$s"
        )
    }

    /**
     * 判断是否同一天
     * @param timestamp1
     * @param timestamp2
     * @return
     */
    fun isSameDay(timestamp1: Long, timestamp2: Long): Boolean {
        val cal1 = Calendar.getInstance()
        cal1.timeInMillis = timestamp1

        val cal2 = Calendar.getInstance()
        cal2.timeInMillis = timestamp2

        return cal1[Calendar.YEAR] == cal2[Calendar.YEAR] &&
                cal1[Calendar.DAY_OF_YEAR] == cal2[Calendar.DAY_OF_YEAR]
    }

    val todayStart: Long
        get() {
            val currentTime: Long = SntpClock.currentTimeMillis()
            return currentTime - currentTime % ONE_DAY_MILLLIS
        }

    /**
     * 根据秒数，计算倒计时展示字符串（分钟：秒）
     * @param seconds
     * @return
     */
    fun getMinuteSecondCountDownTime(seconds: Int): String {
        val minute = seconds / 60
        val second = seconds % 60
        val minuteStr = if (minute < 10) "0$minute" else "" + minute
        val secondStr = if (second < 10) "0$second" else "" + second
        return "$minuteStr:$secondStr"
    }

    fun getTimeBySdf8(time: String?): Long {
        return try {
            sdf8.parse(time).time
        } catch (e: Exception) {
            0
        }
    }
}
