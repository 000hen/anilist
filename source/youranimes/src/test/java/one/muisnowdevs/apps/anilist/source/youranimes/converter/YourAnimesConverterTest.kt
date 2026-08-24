package one.muisnowdevs.apps.anilist.source.youranimes.converter

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.DayOfWeek

/**
 * The upstream page marks a title it has not scheduled by putting a *string* in fields that are
 * otherwise numbers, rather than by omitting them or sending null. Both converters used to treat
 * that as the page having drifted and raised — which failed the whole season's decode, not the one
 * title, so a single unannounced show emptied the schedule.
 *
 * Only weekdays 1..6 are pinned here. 0 and 7 both mean Sunday under one reading of the upstream
 * numbering or the other, and which of the two is right is not settled by the code alone.
 */
class YourAnimesConverterTest {
    @Test
    fun week_readsTheDaysBothNumberingsAgreeOn() {
        val expected = mapOf(
            1 to DayOfWeek.MONDAY,
            2 to DayOfWeek.TUESDAY,
            3 to DayOfWeek.WEDNESDAY,
            4 to DayOfWeek.THURSDAY,
            5 to DayOfWeek.FRIDAY,
            6 to DayOfWeek.SATURDAY,
            7 to DayOfWeek.SUNDAY,
        )

        for ((raw, day) in expected) {
            assertEquals("week $raw", day, Json.decodeFromString(YourAnimesWeekConverter, "$raw"))
        }
    }

    @Test
    fun week_readsTheMarkerStringAsNoWeekday() {
        assertNull(Json.decodeFromString(YourAnimesWeekConverter, "\"未定\""))
    }

    @Test
    fun week_readsNullAsNoWeekday() {
        assertNull(Json.decodeFromString(YourAnimesWeekConverter, "null"))
    }

    /** Total on purpose: a strict read here fails the other few hundred titles in the season too. */
    @Test
    fun week_readsANumberOutsideTheWeekAsNoWeekday() {
        assertNull(Json.decodeFromString(YourAnimesWeekConverter, "99"))
        assertNull(Json.decodeFromString(YourAnimesWeekConverter, "-3"))
    }

    @Test
    fun dateTime_readsTheTimeAsMinutesSinceMidnight() {
        val cases = listOf(
            "2026-07-05 01:30" to "01:30",
            "2026-07-12 02:00" to "02:00",
            "2026-07-05 02:38" to "02:38",
            "2026-04-12 07:00" to "07:00",
            "2026-07-12 07:00" to "07:00",
            "2026-04-12 07:00" to "07:00",
            "2026-04-12 07:00" to "07:00",
            "2025-04-06 08:28" to "08:28",
            "2026-02-01 08:30" to "08:30",
            "2025-10-05 09:00" to "09:00",
            "2026-07-05 16:30" to "16:30",
            "2026-07-05 17:00" to "17:00",
            "2026-07-05 17:30" to "17:30",
            "2026-07-05 22:00" to "22:00",
            "2026-07-05 22:30" to "22:30",
            "2026-07-05 23:00" to "23:00",
            "2026-07-05 23:15" to "23:15",
            "2026-04-05 23:15" to "23:15",
            "2026-04-12 23:30" to "23:30",
            "2026-07-12 23:45" to "23:45",
        )

        for ((rawDateTime, expectedTime) in cases) {
            val (hour, minute) = expectedTime.split(':').map(String::toInt)
            val expectedMinutes = hour * 60 + minute

            assertEquals(
                rawDateTime,
                expectedMinutes,
                Json.decodeFromString(YourAnimesDateToTimeConverter, "\"$rawDateTime\""),
            )
        }
    }

    @Test
    fun minute_readsTheNumberItIsGiven() {
        assertEquals(1410, Json.decodeFromString(YourAnimesMinuteConverter, "1410"))
        assertEquals(0, Json.decodeFromString(YourAnimesMinuteConverter, "0"))
    }

    /**
     * Paired with the weekday on purpose: a title the page has not scheduled tends to carry a marker
     * in both fields, so a strict minute would have kept failing exactly the titles the nullable
     * weekday was added to admit.
     */
    @Test
    fun minute_readsTheMarkerStringAsNoTime() {
        assertNull(Json.decodeFromString(YourAnimesMinuteConverter, "\"未定\""))
        assertNull(Json.decodeFromString(YourAnimesMinuteConverter, "null"))
    }
}
