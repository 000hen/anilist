package one.muisnowdevs.apps.anilist.model

import uniffi.anilist.AnimeSeason

val AnimeSeason.month: Int
    get() = when (this) {
        AnimeSeason.WINTER -> 1
        AnimeSeason.SPRING -> 4
        AnimeSeason.SUMMER -> 7
        AnimeSeason.FALL -> 10
    }

fun AnimeSeason.Companion.fromMonth(month: Int): AnimeSeason = when (month) {
    in 1..3 -> AnimeSeason.WINTER
    in 4..6 -> AnimeSeason.SPRING
    in 7..9 -> AnimeSeason.SUMMER
    in 10..12 -> AnimeSeason.FALL
    else -> error("Invalid month: $month")
}
