package one.muisnowdevs.apps.anilist.source

interface AnimeSource {
    suspend fun load(year: Int, season: Season): Map<Week, List<AnimeInformation>>
}