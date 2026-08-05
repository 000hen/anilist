package one.muisnowdevs.apps.anilist.source

import java.time.Year

interface AnimeSource {
    suspend fun list(year: Year, season: AnilistSeason): Map<ScheduleDay, List<AnilistAnime>>
}