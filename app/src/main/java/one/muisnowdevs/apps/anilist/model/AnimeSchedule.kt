package one.muisnowdevs.apps.anilist.model

import uniffi.anilist.Anime
import uniffi.anilist.AnimeTime
import uniffi.anilist.Weekday
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId

// Keep the canonical record intact; only the display time depends on this device.
data class ScheduledAnime(val anime: Anime, val localTime: WeekTime?)

fun AnimeTime.toLocalWeekTime(
    targetZone: ZoneId,
    referenceDate: LocalDate,
): WeekTime = WeekTime(
    week = when (week) {
        Weekday.MONDAY -> DayOfWeek.MONDAY
        Weekday.TUESDAY -> DayOfWeek.TUESDAY
        Weekday.WEDNESDAY -> DayOfWeek.WEDNESDAY
        Weekday.THURSDAY -> DayOfWeek.THURSDAY
        Weekday.FRIDAY -> DayOfWeek.FRIDAY
        Weekday.SATURDAY -> DayOfWeek.SATURDAY
        Weekday.SUNDAY -> DayOfWeek.SUNDAY
    },
    minute = minute?.value?.toInt(),
    zone = ZoneId.of(zone),
).toZone(targetZone, referenceDate)

fun List<Anime>.toSchedule(
    targetZone: ZoneId = ZoneId.systemDefault(),
    referenceDate: LocalDate = LocalDate.now(),
): Map<ScheduleDay, List<ScheduledAnime>> = map { anime ->
    ScheduledAnime(anime, anime.onAirTime?.toLocalWeekTime(targetZone, referenceDate))
}.sortedWith(compareBy(nullsLast(WeekTime.AIRING_ORDER)) { it.localTime })
    .groupBy { ScheduleDay.of(it.localTime?.week) }
