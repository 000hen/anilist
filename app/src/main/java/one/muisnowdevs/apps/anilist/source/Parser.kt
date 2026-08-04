package one.muisnowdevs.apps.anilist.source

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import one.muisnowdevs.apps.anilist.converter.WeekConverter

@Serializable
data class AnimeInformation(
    @SerialName("_id")
    val id: String,

    val adultContent: Boolean = false,
    val adultstreaming: List<Streaming>,
    val airedEps: Int? = null,
    val aliases: List<String> = emptyList(),
    val aniType: String,
    val cast: List<Cast>,
    val commentCount: Long,
    val copyright: String? = null,
    val cover: String,
    val cross: Boolean,
    val date: String,

    @Serializable(with = WeekConverter::class)
    val dayOfWeek: Week,

    val description: String,
    val durationDesc: String? = null,
    val enableVoting: Boolean,
    val episode: String,
    val favorability: Favorability,
    val hasNews: Boolean,
    val isFavorite: Boolean,
    val jpName: String,
    val name: String,
    val newsDate: String,
    val olinks: List<SocialLink>,
    val playDate: String,
    val playEps: Long,
    val playTotal: Long,
    val scheduleDate: String,
    val seasonItemDateModified: String? = null,
    val seriesStatus: String,
    val songs: List<Song>,
    val staff: List<Staff>,
    val status: String,
    val streaming: List<Streaming>,
    val studios: List<Studio>,
    val tags: Map<String, Int>,
    val twAgent: String,
    val updatedTimestamp: String,

    @SerialName("_weekMinutes")
    val timeInDay: Int
) {
    fun getWeeklyTime(): Int = dayOfWeek.weekNumber * 1440 + timeInDay
}

@Serializable
data class Cast(
    val name: String,
    val role: String,
    val character: String? = null,
    val voice: String? = null,
)

@Serializable
data class Favorability(
    val average: Double,
    val counts: Long,
)

@Serializable
data class SocialLink(
    val title: String,
    val url: String,
)

@Serializable
data class Song(
    val credits: List<Credit>,
    val title: String,
    val type: String,
)

@Serializable
data class Credit(
    val name: String,
)

@Serializable
data class Staff(
    val credits: List<CreditOwner>,
    val role: String,
    val jobTitle: String? = null,
    val name: String? = null,
)

@Serializable
data class CreditOwner(
    val name: String,
)

@Serializable
data class Streaming(
    val payment: String? = null,
    val title: String,
    val url: String,
    val vendor: String,
    val hasZhCnSubtitle: Boolean? = null,
    val hasZhHkSubtitle: Boolean? = null,
    val adUrl: String? = null,
)

@Serializable
data class Studio(
    val name: String,
)
