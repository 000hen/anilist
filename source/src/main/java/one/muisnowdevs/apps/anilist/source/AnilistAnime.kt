package one.muisnowdevs.apps.anilist.source

data class AnilistAnime(
    val id: String,

    val name: String,
    val onAirTime: WeekTime?,
    val description: String,
    val isAdult: Boolean,

    val image: String?,
    val banner: String?,

    val cast: List<String>,

    val genres: List<String>,
    val streaming: List<AnilistStreaming>,
    val site: List<AnilistSite>,
)
