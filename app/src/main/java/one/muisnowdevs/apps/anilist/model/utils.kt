package one.muisnowdevs.apps.anilist.model

import java.time.LocalDate
import java.util.Calendar


/**
 * Start of today in the same minutes-since-Monday space as [WeekTime.minuteOfWeek].
 *
 * Reads the day's own value rather than stepping the enum back a day: `dayOfWeek - 1` gives the
 * previous day, whose value is one lower every day but Monday, where it wraps to Sunday's 7 and
 * puts the start of today past the end of the week. Nothing was ever counted as aired on a Monday.
 */
fun getMinimalMinute(): Int = (LocalDate.now().dayOfWeek.value - 1) * 1440

fun getCurrentMinute(): Int {
    val calendar = Calendar.getInstance()
    return getMinimalMinute() +
            calendar.get(Calendar.HOUR_OF_DAY) * 60 +
            calendar.get(Calendar.MINUTE)
}
