package one.muisnowdevs.apps.anilist.source

import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import uniffi.anilist.HttpMethod
import uniffi.anilist.HttpRequest
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

fun interface HttpClient {
    suspend fun execute(request: HttpRequest): String
}

class OkHttpTransport(private val client: OkHttpClient = sharedHttp) : HttpClient {
    // Coil already uses OkHttp. Sharing its transport dependency avoids Retrofit and keeps
    // cancellation able to close an in-flight response when the selected season changes.
    override suspend fun execute(request: HttpRequest): String =
        suspendCancellableCoroutine { continuation ->
            val builder = Request.Builder().url(request.url)
            request.headers.forEach { builder.addHeader(it.name, it.value) }
            when (request.method) {
                HttpMethod.GET -> builder.get()
                HttpMethod.POST -> builder.post((request.body ?: "").toRequestBody())
            }
            val call = client.newCall(builder.build())
            continuation.invokeOnCancellation { call.cancel() }
            call.enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    continuation.resumeWithException(e)
                }

                override fun onResponse(call: Call, response: Response) {
                    val result = runCatching {
                        response.use {
                            if (!it.isSuccessful) throw IOException("HTTP ${it.code}: ${request.url}")
                            it.body?.string() ?: throw IOException("Empty response: ${request.url}")
                        }
                    }
                    result.fold(continuation::resume, continuation::resumeWithException)
                }
            })
        }

    private companion object {
        val sharedHttp = OkHttpClient()
    }
}
