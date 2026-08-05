package one.muisnowdevs.apps.anilist.source

import java.time.LocalDate
import java.time.Year
import java.util.Calendar

fun getCurrentSeason(): Pair<Year, AnilistSeason> {
    val year = Year.now()
    val month = LocalDate.now().monthValue

    return year to AnilistSeason.fromMonth(month)
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
    val week = LocalDate.now().dayOfWeek - 1
    return week.value * 1440
}

fun getCurrentMinute(): Int {
    val calendar = Calendar.getInstance()
    return getMinimalMinute() +
            calendar.get(Calendar.HOUR_OF_DAY) * 60 +
            calendar.get(Calendar.MINUTE)
}
