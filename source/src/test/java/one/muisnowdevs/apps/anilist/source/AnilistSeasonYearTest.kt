package one.muisnowdevs.apps.anilist.source

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.Year

class AnilistSeasonYearTest {
    /** The two days the ranges used to disagree about, either side of a season boundary. */
    @Test
    fun of_readsTheSeasonAroundASeasonBoundary() {
        assertEquals(
            AnilistSeasonYear(Year.of(2026), AnilistSeason.WINTER),
            AnilistSeasonYear.of(LocalDate.of(2026, 3, 31))
        )

        assertEquals(
            AnilistSeasonYear(Year.of(2026), AnilistSeason.SPRING),
            AnilistSeasonYear.of(LocalDate.of(2026, 4, 1))
        )
    }

    /** December used to throw out of here, taking the app's first load down with it. */
    @Test
    fun of_readsDecemberAsFall() {
        assertEquals(
            AnilistSeasonYear(Year.of(2026), AnilistSeason.FALL),
            AnilistSeasonYear.of(LocalDate.of(2026, 12, 25))
        )
    }

    /** The season the app opens on has to be one the picker can also step back to. */
    @Test
    fun current_fallsInsideTheBrowsableRange() {
        val current = AnilistSeasonYear.current()

        assertTrue("$current is below ${AnilistSeasonYear.EARLIEST}", current.year >= AnilistSeasonYear.EARLIEST)
        assertTrue("$current is above ${AnilistSeasonYear.latest()}", current.year <= AnilistSeasonYear.latest())
    }
}
