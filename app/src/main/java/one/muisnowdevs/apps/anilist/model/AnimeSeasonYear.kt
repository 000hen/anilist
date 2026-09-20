package one.muisnowdevs.apps.anilist.model

import one.muisnowdevs.apps.anilist.model.AnimeSeasonYear.Companion.current
import uniffi.anilist.AnimeSeason
import java.time.LocalDate
import java.time.Year

/**
 * One schedule page: a season and the year it falls in.
 *
 * The two travel together everywhere they are used — the request is addressed by both, and so is
 * the question of whether what is on screen is the season airing right now — so they are one value
 * rather than two parameters a call site could pair up wrongly.
 */
data class AnimeSeasonYear(val year: Year, val season: AnimeSeason) {
    companion object {
        /** Oldest season offered for browsing; youranimes.tw thins out below this. */
        val EARLIEST: Year = Year.of(2015)

        /**
         * A year past the present, so a season that has been announced but has not begun airing is
         * still reachable.
         */
        fun latest(): Year = Year.now().plusYears(1)

        /**
         * Split out of [current] so the month maths can be exercised against a chosen date rather
         * than only against whatever day the suite happens to run on — which is how the ranges in
         * [AnimeSeason.fromMonth] stayed wrong for four months of the year.
         */
        fun of(date: LocalDate) =
            AnimeSeasonYear(Year.of(date.year), AnimeSeason.fromMonth(date.monthValue))

        fun current(): AnimeSeasonYear = of(LocalDate.now())
    }
}
