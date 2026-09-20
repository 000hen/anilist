package one.muisnowdevs.apps.anilist.source

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import uniffi.anilist.Anime
import uniffi.anilist.AnimeSeason
import uniffi.anilist.NativeAnimeSource

class AnilistClient(sourceId: String, private val http: HttpClient = OkHttpTransport()) {
    private val source by lazy { NativeAnimeSource(sourceId) }

    suspend fun list(year: Int, season: AnimeSeason): List<Anime> {
        require(year in 0..65535) { "Year is outside the native source range: $year" }
        val body = http.execute(source.listRequest(year.toUShort(), season))
        return withContext(Dispatchers.Default) { source.parseList(body) }
    }
}
