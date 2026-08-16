package one.muisnowdevs.apps.anilist.source.youranimes

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import one.muisnowdevs.apps.anilist.source.AnilistAnime
import one.muisnowdevs.apps.anilist.source.AnilistSeason
import one.muisnowdevs.apps.anilist.source.AnimeSource
import one.muisnowdevs.apps.anilist.source.ScheduleDay
import one.muisnowdevs.apps.anilist.source.WeekTime
import one.muisnowdevs.apps.anilist.source.reportTimeElapsed
import org.jsoup.Jsoup
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import java.time.Year

private const val TAG = "YourAnimesSource"

object YourAnimesSource : AnimeSource {
    private interface Endpoint {
        @GET("/bangumi/{time}")
        suspend fun fetchList(@Path("time") time: String): String
    }

    private val json = Json { ignoreUnknownKeys = true }
    private val client = Retrofit.Builder()
        .baseUrl("https://youranimes.tw")
        .addConverterFactory(ScalarsConverterFactory.create())
        .build()
    private val service = client.create(Endpoint::class.java)
    private fun String.extractNextF(): List<String> =
        Jsoup.parse(this)
            .select("script")
            .asSequence()
            .map { it.data().trim() }
            .filter { it.startsWith("self.__next_f.push(") }
            .map {
                it.removePrefix("self.__next_f.push(")
                    .removeSuffix(")")
            }
            .toList()

    private suspend fun loadRaw(path: String): List<AnimeInformation> =
        withContext(Dispatchers.Default) {
            val raw = reportTimeElapsed { service.fetchList(path) }

            val content = reportTimeElapsed {
                val nextF = raw.extractNextF()
                nextF.firstOrNull { content -> content.contains("{\\\"animes\\\":[") }
                    ?: error("Invalid format")
            }

            Log.d(TAG, "first clean: ${content.take(100)}...${content.takeLast(100)}")

            val rawList = Json.parseToJsonElement(content).jsonArray[1].toString()
            Log.d(TAG, "second clean: ${rawList.take(100)}...${rawList.takeLast(100)}")

            val cleaned = rawList.drop(rawList.indexOf(':') + 1)
                .replace("\\\"", "\"")
                .replace("\\\\", "\\")
                .dropLast(3)
            Log.d(TAG, "third clean: ${cleaned.take(100)}...${cleaned.takeLast(100)}")

            val inner = Json.parseToJsonElement(cleaned).jsonArray[3]

            val animes = inner.jsonObject["animes"] ?: error("Invalid JSON")
            val structured = json.decodeFromJsonElement<List<AnimeInformation>>(animes)

            val streamingRaw = inner.jsonObject["nameMap"]
            val streamingMap =
                if (streamingRaw == null) emptyMap()
                else json.decodeFromJsonElement<Map<String, String>>(streamingRaw)

            val final = structured.map { item ->
                item.copy(
                    streaming = item.streaming.map { it.resolveVendor(streamingMap) }
                )
            }

            return@withContext final
        }

    private fun Streaming.resolveVendor(vendors: Map<String, String>): Streaming {
        val newVendor = vendors[vendor] ?: return this
        return copy(vendorLocalName = newVendor)
    }

    override suspend fun list(
        year: Year,
        season: AnilistSeason
    ): Map<ScheduleDay, List<AnilistAnime>> {
        val path = "${year.value}${season.month.toString().padStart(2, '0')}"
        val raw = loadRaw(path)

        return raw
            .map { it.toAnilistAnime() }
            .sortedWith(compareBy(nullsLast(WeekTime.AIRING_ORDER)) { it.onAirTime })
            .groupBy { ScheduleDay.of(it.onAirTime?.week) }
    }
}