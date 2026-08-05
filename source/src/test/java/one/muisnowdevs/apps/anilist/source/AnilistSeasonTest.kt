package one.muisnowdevs.apps.anilist.source

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * These ranges were zero-based while every caller handed over a one-based month, which put March,
 * June and September in the season after the right one and left December matching nothing at all.
 *
 * All twelve months are pinned rather than the boundaries alone, because it was the boundaries that
 * moved and a test written around them would have moved with them.
 */
class AnilistSeasonTest {
    @Test
    fun fromMonth_mapsEveryMonthToItsSeason() {
        val expected = mapOf(
            1 to AnilistSeason.WINTER,
            2 to AnilistSeason.WINTER,
            3 to AnilistSeason.WINTER,
            4 to AnilistSeason.SPRING,
            5 to AnilistSeason.SPRING,
            6 to AnilistSeason.SPRING,
            7 to AnilistSeason.SUMMER,
            8 to AnilistSeason.SUMMER,
            9 to AnilistSeason.SUMMER,
            10 to AnilistSeason.FALL,
            11 to AnilistSeason.FALL,
            12 to AnilistSeason.FALL,
        )

        for ((month, season) in expected) {
            assertEquals("month $month", season, AnilistSeason.fromMonth(month))
        }
    }

    /** Each season also names the month it opens in, so the two have to agree both ways round. */
    @Test
    fun fromMonth_agreesWithTheMonthEachSeasonStartsIn() {
        for (season in AnilistSeason.entries) {
            assertEquals(season, AnilistSeason.fromMonth(season.month))
        }
    }

    @Test(expected = IllegalStateException::class)
    fun fromMonth_rejectsMonthBelowJanuary() {
        AnilistSeason.fromMonth(0)
    }

    @Test(expected = IllegalStateException::class)
    fun fromMonth_rejectsMonthAboveDecember() {
        AnilistSeason.fromMonth(13)
    }
}
