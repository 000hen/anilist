package one.muisnowdevs.apps.anilist.source.youranimes

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import one.muisnowdevs.apps.anilist.source.AnilistAnime
import one.muisnowdevs.apps.anilist.source.AnilistSeason
import one.muisnowdevs.apps.anilist.source.AnimeSource
import one.muisnowdevs.apps.anilist.source.getCurrentSessionString
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import java.time.DayOfWeek
import java.time.Year

private const val TAG = "YourAnimesSource"

object YourAnimesSource : AnimeSource {
    private interface Endpoint {
        @GET("/bangumi/{time}")
        suspend fun fetchList(@Path("time") time: String = getCurrentSessionString()): String
    }

    private val json = Json { ignoreUnknownKeys = true }
    private val client = Retrofit.Builder()
        .baseUrl("https://youranimes.tw")
        .addConverterFactory(ScalarsConverterFactory.create())
        .build()
    private val service = client.create(Endpoint::class.java)

    private suspend fun loadRaw(path: String): String = withContext(Dispatchers.Default) {
        val raw = service.fetchList(path)

        val startLast = raw.lastIndexOf("<script>self.__next_f.push")
        val start = raw.lastIndexOf("<script>self.__next_f.push", startLast - 1)
        val end = raw.indexOf("</script>", start)
        val content = raw
            .substring(start, end)
            .replace("<script>self.__next_f.push(", "")
            .dropLast(1)

        Log.d(TAG, "first clean: ${content.take(100)}...")

        val rawList = Json.parseToJsonElement(content).jsonArray[1].toString()
        Log.d(TAG, "second clean: ${rawList.take(100)}...")

        val cleaned = rawList.drop(rawList.indexOf(':') + 1)
            .replace("\\\"", "\"")
            .replace("\\\\", "\\")
            .dropLast(3)
        Log.d(TAG, "third clean: ${cleaned.take(100)}...")

        val contentInner = Json.parseToJsonElement(cleaned)
            .jsonArray[3]
            .jsonObject["children"]
            ?.jsonArray[2]
            ?.jsonArray[3]
            ?.jsonObject["animes"] ?: error("Invalid JSON")

        return@withContext contentInner.toString()
    }

    override suspend fun list(
        year: Year,
        season: AnilistSeason
    ): Map<DayOfWeek, List<AnilistAnime>> {
        val path = "${year.value}${season.month.toString().padStart(2, '0')}"
        val raw = loadRaw(path)

        val list = json.decodeFromString<List<AnimeInformation>>(raw)
        return list
            .sortedBy { it.getWeeklyTime() }
            .map { it.toAnilistAnime() }
            .groupBy { it.onAirTime.week }
    }
}