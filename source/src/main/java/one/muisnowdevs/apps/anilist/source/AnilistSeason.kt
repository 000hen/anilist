package one.muisnowdevs.apps.anilist.source

enum class AnilistSeason(val month: Int) {
    WINTER(1),
    SPRING(4),
    SUMMER(7),
    FALL(10);

    companion object {
        fun fromMonth(month: Int): AnilistSeason = when (month) {
            in 0..2 -> WINTER
            in 3..5 -> SPRING
            in 6..8 -> SUMMER
            in 9..11 -> FALL
            else -> error("Invalid month")
        }
    }
}