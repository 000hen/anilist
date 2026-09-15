package one.muisnowdevs.apps.anilist.source.youranimes

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import one.muisnowdevs.apps.anilist.source.AnilistAnime
import one.muisnowdevs.apps.anilist.source.AnilistSeason
import one.muisnowdevs.apps.anilist.source.AnilistSite
import one.muisnowdevs.apps.anilist.source.AnilistStreaming
import one.muisnowdevs.apps.anilist.source.AnimeSource
import one.muisnowdevs.apps.anilist.source.ScheduleDay
import one.muisnowdevs.apps.anilist.source.WeekTime
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import uniffi.anilist.NativeAnimeParser
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.Year
import java.time.ZoneId
import uniffi.anilist.Anime as RustAnime
import uniffi.anilist.Weekday as RustWeekday

object YourAnimesSource : AnimeSource {
    private interface Endpoint {
        @GET("/bangumi/{time}")
        suspend fun fetchList(
            @Path("time") time: String,
        ): String
    }

    private val service = Retrofit.Builder()
        .baseUrl("https://youranimes.tw")
        .addConverterFactory(ScalarsConverterFactory.create())
        .build()
        .create(Endpoint::class.java)

    private val parser by lazy {
        NativeAnimeParser("youranimes")
    }

    override suspend fun list(
        year: Year,
        season: AnilistSeason,
    ): Map<ScheduleDay, List<AnilistAnime>> {
        val path = buildString {
            append(year.value)
            append(season.month.toString().padStart(2, '0'))
        }

        val body = service.fetchList(path)
        val parsed = withContext(Dispatchers.Default) { parser.parseList(body) }

        val targetZone = ZoneId.systemDefault()
        val referenceDate = LocalDate.now()

        return parsed
            .map { anime ->
                anime.toDomain(
                    targetZone = targetZone,
                    referenceDate = referenceDate,
                )
            }
            .sortedWith(
                compareBy(nullsLast(WeekTime.AIRING_ORDER)) {
                    it.onAirTime
                }
            )
            .groupBy { ScheduleDay.of(it.onAirTime?.week) }
    }
}

private fun RustAnime.toDomain(
    targetZone: ZoneId,
    referenceDate: LocalDate,
): AnilistAnime = AnilistAnime(
    id = id,
    name = name,
    description = description,

    onAirTime = onAirTime?.let { time ->
        WeekTime(
            week = time.week.toDayOfWeek(),
            minute = time.minute?.value?.toInt(),
            zone = ZoneId.of(time.zone),
        ).toZone(
            targetZone = targetZone,
            referenceDate = referenceDate,
        )
    },

    isAdult = isAdult,
    image = image,
    banner = banner,
    cast = cast,
    genres = genres,

    streaming = streaming.map { stream ->
        AnilistStreaming(
            name = stream.name,
            url = stream.url,
            logo = stream.logo,
        )
    },

    site = sites.map { site ->
        AnilistSite(
            title = site.title,
            url = site.url,
        )
    },
)

private fun RustWeekday.toDayOfWeek(): DayOfWeek = when (this) {
    RustWeekday.MONDAY -> DayOfWeek.MONDAY
    RustWeekday.TUESDAY -> DayOfWeek.TUESDAY
    RustWeekday.WEDNESDAY -> DayOfWeek.WEDNESDAY
    RustWeekday.THURSDAY -> DayOfWeek.THURSDAY
    RustWeekday.FRIDAY -> DayOfWeek.FRIDAY
    RustWeekday.SATURDAY -> DayOfWeek.SATURDAY
    RustWeekday.SUNDAY -> DayOfWeek.SUNDAY
}