package one.muisnowdevs.apps.anilist.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import uniffi.anilist.AnimeSeason
import java.time.LocalDate
import java.time.Year

class AnimeSeasonYearTest {
    /** The two days the ranges used to disagree about, either side of a season boundary. */
    @Test
    fun of_readsTheSeasonAroundASeasonBoundary() {
        assertEquals(
            AnimeSeasonYear(Year.of(2026), AnimeSeason.WINTER),
            AnimeSeasonYear.of(LocalDate.of(2026, 3, 31))
        )

        assertEquals(
            AnimeSeasonYear(Year.of(2026), AnimeSeason.SPRING),
            AnimeSeasonYear.of(LocalDate.of(2026, 4, 1))
        )
    }

    /** December used to throw out of here, taking the app's first load down with it. */
    @Test
    fun of_readsDecemberAsFall() {
        assertEquals(
            AnimeSeasonYear(Year.of(2026), AnimeSeason.FALL),
            AnimeSeasonYear.of(LocalDate.of(2026, 12, 25))
        )
    }

    /** The season the app opens on has to be one the picker can also step back to. */
    @Test
    fun current_fallsInsideTheBrowsableRange() {
        val current = AnimeSeasonYear.current()

        assertTrue(
            "$current is below ${AnimeSeasonYear.EARLIEST}",
            current.year >= AnimeSeasonYear.EARLIEST
        )
        assertTrue(
            "$current is above ${AnimeSeasonYear.latest()}",
            current.year <= AnimeSeasonYear.latest()
        )
    }
}
