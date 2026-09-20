package one.muisnowdevs.apps.anilist.model

import org.junit.Assert.assertEquals
import org.junit.Test
import uniffi.anilist.AnimeSeason

/**
 * These ranges were zero-based while every caller handed over a one-based month, which put March,
 * June and September in the season after the right one and left December matching nothing at all.
 *
 * All twelve months are pinned rather than the boundaries alone, because it was the boundaries that
 * moved and a test written around them would have moved with them.
 */
class AnimeSeasonTest {
    @Test
    fun fromMonth_mapsEveryMonthToItsSeason() {
        val expected = mapOf(
            1 to AnimeSeason.WINTER,
            2 to AnimeSeason.WINTER,
            3 to AnimeSeason.WINTER,
            4 to AnimeSeason.SPRING,
            5 to AnimeSeason.SPRING,
            6 to AnimeSeason.SPRING,
            7 to AnimeSeason.SUMMER,
            8 to AnimeSeason.SUMMER,
            9 to AnimeSeason.SUMMER,
            10 to AnimeSeason.FALL,
            11 to AnimeSeason.FALL,
            12 to AnimeSeason.FALL,
        )

        for ((month, season) in expected) {
            assertEquals("month $month", season, AnimeSeason.fromMonth(month))
        }
    }

    /** Each season also names the month it opens in, so the two have to agree both ways round. */
    @Test
    fun fromMonth_agreesWithTheMonthEachSeasonStartsIn() {
        for (season in AnimeSeason.entries) {
            assertEquals(season, AnimeSeason.fromMonth(season.month))
        }
    }

    @Test(expected = IllegalStateException::class)
    fun fromMonth_rejectsMonthBelowJanuary() {
        AnimeSeason.fromMonth(0)
    }

    @Test(expected = IllegalStateException::class)
    fun fromMonth_rejectsMonthAboveDecember() {
        AnimeSeason.fromMonth(13)
    }
}
