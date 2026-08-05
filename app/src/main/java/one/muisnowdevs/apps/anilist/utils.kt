package one.muisnowdevs.apps.anilist

import android.util.Log
import java.time.DayOfWeek
import java.time.LocalDate
import kotlin.time.Duration.Companion.minutes

/** Renders "minutes past midnight" as the `@HH:mm` badge shown next to a title. */
fun formatTimeInDay(minutesInDay: Int?): String =
    minutesInDay?.minutes?.toComponents { hour, minute, _, _ ->
        "@${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}"
    } ?: "時間未定"

fun <T> List<T>.rotate(startIndex: Int): List<T> {
    require(isNotEmpty())
    val index = startIndex.mod(size)
    return drop(index) + take(index)
}

fun getTodayOrder(): List<DayOfWeek> {
    val dayOfWeek = LocalDate.now().dayOfWeek
    val rotate = listOf(
        DayOfWeek.SUNDAY,
        DayOfWeek.MONDAY,
        DayOfWeek.TUESDAY,
        DayOfWeek.WEDNESDAY,
        DayOfWeek.THURSDAY,
        DayOfWeek.FRIDAY,
        DayOfWeek.SATURDAY,
    ).rotate(dayOfWeek.value)

    Log.d("utils", "RESULT: $rotate, today: $dayOfWeek")

    return rotate
}
