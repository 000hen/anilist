package one.muisnowdevs.apps.anilist.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test
import uniffi.anilist.Anime
import uniffi.anilist.AnimeTime
import uniffi.anilist.Minute
import uniffi.anilist.Weekday
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId

class AnimeScheduleTest {
    @Test
    fun groupsAndSortsByLocalTimeWithoutCopyingCanonicalRecords() {
        val midnight = anime("midnight", Weekday.MONDAY, 30)
        val unknown = anime("unknown", null, null)
        val unfixed = anime("unfixed", Weekday.SUNDAY, null)
        val evening = anime("evening", Weekday.SUNDAY, 1380)
        val schedule = listOf(unknown, unfixed, midnight, evening).toSchedule(
            ZoneId.of("Asia/Taipei"), LocalDate.of(2026, 8, 5),
        )

        val sunday = schedule.getValue(ScheduleDay.Of(DayOfWeek.SUNDAY))
        assertEquals(listOf("evening", "midnight", "unfixed"), sunday.map { it.anime.id })
        assertEquals(1410, sunday[1].localTime?.minute)
        assertSame(midnight, sunday[1].anime)
        assertEquals(Weekday.MONDAY, midnight.onAirTime?.week)
        assertEquals(listOf(unknown), schedule.getValue(ScheduleDay.Undetermined).map { it.anime })
    }

    private fun anime(id: String, day: Weekday?, minute: Int?) = Anime(
        id, id, "", day?.let { AnimeTime(it, minute?.let { Minute(it.toUShort()) }, "Asia/Tokyo") },
        false, null, null, emptyList(), emptyList(), emptyList(), emptyList(),
    )
}
