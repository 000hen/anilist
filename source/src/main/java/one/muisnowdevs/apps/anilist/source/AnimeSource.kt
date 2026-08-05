package one.muisnowdevs.apps.anilist.source

import java.time.DayOfWeek
import java.time.Year

interface AnimeSource {
    suspend fun list(year: Year, season: AnilistSeason): Map<DayOfWeek, List<AnilistAnime>>
}