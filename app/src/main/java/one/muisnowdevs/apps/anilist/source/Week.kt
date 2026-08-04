package one.muisnowdevs.apps.anilist.source

import java.time.DayOfWeek
import java.time.LocalDate

enum class Week(val week: DayOfWeek, val weekNumber: Int) {
    MONDAY(DayOfWeek.MONDAY, 1),
    TUESDAY(DayOfWeek.TUESDAY, 2),
    WEDNESDAY(DayOfWeek.WEDNESDAY, 3),
    THURSDAY(DayOfWeek.THURSDAY, 4),
    FRIDAY(DayOfWeek.FRIDAY, 5),
    SATURDAY(DayOfWeek.SATURDAY, 6),
    SUNDAY(DayOfWeek.SUNDAY, 0);

    companion object {
        fun fromInt(value: Int) = entries.firstOrNull { it.weekNumber == value }
        fun fromString(value: String): Week {
            require(value.length == 1)
            require(value.first().isDigit())

            val number = value.toInt()
            require(number in 1..7)

            return fromInt(number) ?: error("Invalid week number")
        }

        fun getToday(): Week = entries.first { it.week == DayOfWeek.from(LocalDate.now()) }
    }
}