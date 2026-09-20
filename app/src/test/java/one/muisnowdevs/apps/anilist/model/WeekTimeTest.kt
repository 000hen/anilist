package one.muisnowdevs.apps.anilist.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId

/**
 * A null minute is a value here, not a violation — the upstream page announces titles before it
 * fixes their slot. The guard used to read `minute in 0..1439`, which answers false for a null
 * rather than skipping it, so constructing one of those threw and took the season's decode with it.
 *
 * The rest pins the two spaces that null had to be taught: [WeekTime.minuteOfWeek], which answers
 * "where in the week" and now has nothing to answer with, and [WeekTime.sortKey], which has to
 * answer anyway and puts the unfixed slot below its own day without reaching the next one.
 */
class WeekTimeTest {
    private val monday = DayOfWeek.MONDAY
    private val tokyo = ZoneId.of("Asia/Tokyo")
    private val taipei = ZoneId.of("Asia/Taipei")

    @Test
    fun construction_acceptsAnUnfixedMinute() {
        val time = WeekTime(monday, null, tokyo)

        assertEquals(monday, time.week)
        assertNull(time.minute)
    }

    @Test
    fun construction_acceptsBothEndsOfTheDay() {
        assertEquals(0, WeekTime(monday, 0, tokyo).minute)
        assertEquals(1439, WeekTime(monday, 1439, tokyo).minute)
    }

    @Test(expected = IllegalArgumentException::class)
    fun construction_rejectsMinutePastTheEndOfTheDay() {
        WeekTime(monday, 1440, tokyo)
    }

    @Test(expected = IllegalArgumentException::class)
    fun construction_rejectsNegativeMinute() {
        WeekTime(monday, -1, tokyo)
    }

    @Test
    fun minuteOfWeek_countsFromMonday() {
        assertEquals(0, WeekTime(DayOfWeek.MONDAY, 0, tokyo).minuteOfWeek)
        assertEquals(90, WeekTime(DayOfWeek.MONDAY, 90, tokyo).minuteOfWeek)
        assertEquals(1440, WeekTime(DayOfWeek.TUESDAY, 0, tokyo).minuteOfWeek)
        assertEquals(6 * 1440 + 1439, WeekTime(DayOfWeek.SUNDAY, 1439, tokyo).minuteOfWeek)
    }

    /** -1 was still a position, and read as "aired at some point before Monday". */
    @Test
    fun minuteOfWeek_isAbsentWhenTheSlotIsNotFixed() {
        assertNull(WeekTime(DayOfWeek.MONDAY, null, tokyo).minuteOfWeek)
    }

    /**
     * The case that killed the single-integer version of this: a key of 1440 for "no time yet" is
     * not below Tuesday's first minute, it *is* Tuesday's first minute, so an unfixed Monday and a
     * Tuesday midnight tied and either could come first.
     */
    @Test
    fun airingOrder_putsAnUnfixedSlotBelowItsOwnDayAndAboveTheNext() {
        val ordered = listOf(
            WeekTime(DayOfWeek.TUESDAY, 0, tokyo),
            WeekTime(DayOfWeek.MONDAY, null, tokyo),
            WeekTime(DayOfWeek.MONDAY, 1439, tokyo),
            WeekTime(DayOfWeek.MONDAY, 0, tokyo),
        ).sortedWith(WeekTime.AIRING_ORDER)

        assertEquals(
            listOf(
                WeekTime(DayOfWeek.MONDAY, 0, tokyo),
                WeekTime(DayOfWeek.MONDAY, 1439, tokyo),
                WeekTime(DayOfWeek.MONDAY, null, tokyo),
                WeekTime(DayOfWeek.TUESDAY, 0, tokyo),
            ),
            ordered
        )
    }

    @Test
    fun airingOrder_runsFromMondayToSunday() {
        val ordered = listOf(
            WeekTime(DayOfWeek.SUNDAY, 600, tokyo),
            WeekTime(DayOfWeek.WEDNESDAY, 600, tokyo),
            WeekTime(DayOfWeek.MONDAY, 600, tokyo),
        ).sortedWith(WeekTime.AIRING_ORDER)

        assertEquals(
            listOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.SUNDAY),
            ordered.map { it.week }
        )
    }

    @Test
    fun toZone_movesTheTimeWithoutChangingTheDay() {
        val converted = WeekTime(DayOfWeek.WEDNESDAY, 22 * 60, tokyo)
            .toZone(taipei, LocalDate.of(2026, 8, 5))

        assertEquals(DayOfWeek.WEDNESDAY, converted.week)
        assertEquals(21 * 60, converted.minute)
        assertEquals(taipei, converted.zone)
    }

    /**
     * The case the sort in `YourAnimesSource` had to be moved after the conversion for: an hour back
     * from Tokyo's Monday 00:30 is the Sunday before it, so the title changes weekday as well as
     * time and belongs at the *end* of that Sunday rather than the start of it.
     */
    @Test
    fun toZone_walksBackAcrossMidnightIntoThePreviousDay() {
        val converted = WeekTime(DayOfWeek.MONDAY, 30, tokyo)
            .toZone(taipei, LocalDate.of(2026, 8, 5))

        assertEquals(DayOfWeek.SUNDAY, converted.week)
        assertEquals(23 * 60 + 30, converted.minute)
        assertEquals(6 * 1440 + 23 * 60 + 30, converted.minuteOfWeek)
    }

    @Test
    fun toZone_walksForwardAcrossMidnightIntoTheNextDay() {
        val converted = WeekTime(DayOfWeek.SUNDAY, 23 * 60 + 30, taipei)
            .toZone(tokyo, LocalDate.of(2026, 8, 5))

        assertEquals(DayOfWeek.MONDAY, converted.week)
        assertEquals(30, converted.minute)
    }

    /**
     * With no time there is no instant to re-anchor, so the day is carried across and only the zone
     * it is read in moves. Re-anchoring would have to invent a slot to shift.
     */
    @Test
    fun toZone_keepsTheDayWhenThereIsNoSlotToShift() {
        val converted = WeekTime(DayOfWeek.MONDAY, null, tokyo)
            .toZone(taipei, LocalDate.of(2026, 8, 5))

        assertEquals(DayOfWeek.MONDAY, converted.week)
        assertNull(converted.minute)
        assertEquals(taipei, converted.zone)
    }
}
