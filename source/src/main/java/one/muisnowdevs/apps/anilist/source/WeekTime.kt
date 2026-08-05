package one.muisnowdevs.apps.anilist.source

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters

/**
 * A weekday, and the time on it once that is known.
 *
 * [week] stays non-null so nothing here has to invent a position for a title that has none: a title
 * with no weekday at all carries no [WeekTime], and that absence is what [ScheduleDay.of] groups
 * into [ScheduleDay.Undetermined]. A null [minute] is the narrower case — the day is announced but
 * the slot on it is not — and stays under its own weekday.
 */
data class WeekTime(
    val week: DayOfWeek,
    val minute: Int?,
    val zone: ZoneId = ZoneId.systemDefault(),
) {
    init {
        require(minute == null || minute in 0..1439) {
            "Minute must be null or between 0 and 1439, was $minute"
        }
    }

    val minuteOfWeek: Int?
        get() = minute?.let { (week.value - 1) * 1440 + it }

    fun toZone(
        targetZone: ZoneId,
        referenceDate: LocalDate
    ): WeekTime {
        if (minute == null) return WeekTime(week, null, targetZone)

        val hour = minute / 60
        val minuteOfHour = minute % 60

        val sourceDate = referenceDate
            .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            .plusDays((week.value - 1).toLong())

        val source = sourceDate
            .atTime(hour, minuteOfHour)
            .atZone(zone)

        val target = source.withZoneSameInstant(targetZone)

        return WeekTime(
            week = target.dayOfWeek,
            minute = target.hour * 60 + target.minute,
            zone = targetZone
        )
    }

    companion object {
        val AIRING_ORDER: Comparator<WeekTime> =
            compareBy({ it.week }, { it.minute ?: Int.MAX_VALUE })
    }
}
