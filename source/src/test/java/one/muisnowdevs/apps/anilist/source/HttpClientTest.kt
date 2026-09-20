package one.muisnowdevs.apps.anilist.source

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.SocketPolicy
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import uniffi.anilist.HttpHeader
import uniffi.anilist.HttpMethod
import uniffi.anilist.HttpRequest
import java.io.IOException
import java.util.concurrent.TimeUnit

class HttpClientTest {
    @Test
    fun executesSourceIndependentRequestsAndRejectsHttpErrors() = runBlocking {
        MockWebServer().use { server ->
            server.enqueue(MockResponse().setBody("中文"))
            server.enqueue(MockResponse().setResponseCode(503))
            val transport = OkHttpTransport()
            val request = HttpRequest(
                HttpMethod.POST, server.url("/any-source").toString(),
                listOf(
                    HttpHeader("X-Test", "value"),
                    HttpHeader("Content-Type", "application/json")
                ),
                "{\"query\":\"anime\"}",
            )
            assertEquals("中文", transport.execute(request))
            val received = server.takeRequest(5, TimeUnit.SECONDS)!!
            assertEquals("POST", received.method)
            assertEquals("value", received.getHeader("X-Test"))
            assertEquals("application/json", received.getHeader("Content-Type"))
            assertEquals(request.body, received.body.readUtf8())

            val failure = runCatching {
                transport.execute(
                    request.copy(
                        method = HttpMethod.GET,
                        body = null
                    )
                )
            }
            assertTrue(failure.exceptionOrNull() is IOException)
            assertTrue(failure.exceptionOrNull()!!.message!!.contains("503"))
        }
    }

    @Test
    fun cancellationCancelsTheUnderlyingCall() = runBlocking {
        MockWebServer().use { server ->
            server.enqueue(MockResponse().setSocketPolicy(SocketPolicy.NO_RESPONSE))
            val client = OkHttpClient()
            val pending = async {
                OkHttpTransport(client).execute(
                    HttpRequest(HttpMethod.GET, server.url("/slow").toString(), emptyList(), null),
                )
            }
            withContext(Dispatchers.IO) {
                checkNotNull(server.takeRequest(5, TimeUnit.SECONDS))
            }
            val call = client.dispatcher.runningCalls().single()
            pending.cancelAndJoin()
            assertTrue(call.isCanceled())
        }
    }
}
