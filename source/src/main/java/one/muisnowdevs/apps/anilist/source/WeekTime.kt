package one.muisnowdevs.apps.anilist.source

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters

data class WeekTime(
    val week: DayOfWeek,
    val minute: Int,
    val zone: ZoneId = ZoneId.systemDefault(),
) {
    val minuteOfWeek: Int
        get() = (week.value - 1) * 1440 + minute

    init {
        require(minute in 0..1439) { "Minute must be between 0 and 1439" }
    }

    fun toZone(
        targetZone: ZoneId,
        referenceDate: LocalDate
    ): WeekTime {
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
}
