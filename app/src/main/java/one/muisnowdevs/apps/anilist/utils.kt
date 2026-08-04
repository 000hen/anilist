package one.muisnowdevs.apps.anilist

import android.util.Log
import one.muisnowdevs.apps.anilist.source.Season
import one.muisnowdevs.apps.anilist.source.Week
import java.time.LocalDate
import java.util.Calendar
import kotlin.time.Duration.Companion.minutes

fun getCurrentSession(): Pair<Int, Season> {
    val calendar = Calendar.getInstance()
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH) + 1

    return year to Season.fromMonth(month)
}

fun getCurrentSessionString(): String {
    val calendar = Calendar.getInstance()
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)

    return when (month) {
        in 0..2 -> "${year}01"
        in 3..5 -> "${year}04"
        in 6..8 -> "${year}07"
        in 9..11 -> "${year}10"
        else -> error("Invalid month")
    }
}

fun getMinimalMinute(): Int {
    val week = Week.getToday()
    return week.weekNumber * 1440
}

fun getCurrentMinute(): Int {
    val calendar = Calendar.getInstance()
    return getMinimalMinute() +
            calendar.get(Calendar.HOUR_OF_DAY) * 60 +
            calendar.get(Calendar.MINUTE)
}

/** Renders "minutes past midnight" as the `@HH:mm` badge shown next to a title. */
fun formatTimeInDay(minutesInDay: Int): String =
    minutesInDay.minutes.toComponents { hour, minute, _, _ ->
        "@${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}"
    }

fun <T> List<T>.rotate(startIndex: Int): List<T> {
    require(isNotEmpty())
    val index = startIndex.mod(size)
    return drop(index) + take(index)
}

fun getTodayOrder(): List<Week> {
    val dayOfWeek = LocalDate.now().dayOfWeek
    val rotate = listOf(
        Week.SUNDAY,
        Week.MONDAY,
        Week.TUESDAY,
        Week.WEDNESDAY,
        Week.THURSDAY,
        Week.FRIDAY,
        Week.SATURDAY,
    ).rotate(dayOfWeek.value)

    Log.d("utils", "RESULT: $rotate, today: $dayOfWeek")

    return rotate
}
