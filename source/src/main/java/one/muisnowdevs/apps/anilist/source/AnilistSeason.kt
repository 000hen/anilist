package one.muisnowdevs.apps.anilist.source

enum class AnilistSeason(val month: Int) {
    WINTER(1),
    SPRING(4),
    SUMMER(7),
    FALL(10);

    companion object {
        /**
         * One-based to agree with [java.time.LocalDate.monthValue] and with [month] itself. The
         * ranges used to be zero-based while every caller handed over a one-based month, which slid
         * March, June and September into the season after the right one and left December matching
         * nothing at all.
         */
        fun fromMonth(month: Int): AnilistSeason = when (month) {
            in 1..3 -> WINTER
            in 4..6 -> SPRING
            in 7..9 -> SUMMER
            in 10..12 -> FALL
            else -> error("Invalid month: $month")
        }
    }
}
