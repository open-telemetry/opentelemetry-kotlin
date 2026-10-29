package io.opentelemetry.kotlin.instrumentation.ktor.client

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.request.get
import io.ktor.client.request.request
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.opentelemetry.kotlin.OpenTelemetry
import io.opentelemetry.kotlin.createOpenTelemetry
import io.opentelemetry.kotlin.exceptionType
import io.opentelemetry.kotlin.semconv.ErrorAttributes.ERROR_TYPE
import io.opentelemetry.kotlin.semconv.HttpAttributes.HTTP_REQUEST_METHOD
import io.opentelemetry.kotlin.semconv.HttpAttributes.HTTP_REQUEST_METHOD_ORIGINAL
import io.opentelemetry.kotlin.semconv.HttpAttributes.HTTP_RESPONSE_STATUS_CODE
import io.opentelemetry.kotlin.semconv.ServerAttributes.SERVER_ADDRESS
import io.opentelemetry.kotlin.semconv.ServerAttributes.SERVER_PORT
import io.opentelemetry.kotlin.semconv.UrlAttributes.URL_FULL
import io.opentelemetry.kotlin.tracing.SpanKind
import io.opentelemetry.kotlin.tracing.StatusCode
import io.opentelemetry.kotlin.tracing.data.SpanData
import io.opentelemetry.kotlin.tracing.export.FakeSpanProcessor
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

internal class OpenTelemetryKtorClientPluginTest {

    private lateinit var processor: FakeSpanProcessor

    private val spans: List<SpanData>
        get() = processor.endCalls.map { it.toSpanData() }

    @BeforeTest
    fun setUp() {
        processor = FakeSpanProcessor()
    }

    @Test
    fun testRequestSuccess() = runTest {
        client(okEngine()).get("https://example.com/users?id=1")

        val span = spans.single()
        assertEquals("GET", span.name)
        assertEquals(SpanKind.CLIENT, span.spanKind)
        assertEquals(StatusCode.UNSET, span.status.statusCode)
        val expected = mapOf(
            HTTP_REQUEST_METHOD to "GET",
            URL_FULL to "https://example.com/users?id=1",
            SERVER_ADDRESS to "example.com",
            SERVER_PORT to 443L,
            HTTP_RESPONSE_STATUS_CODE to 200L,
        )
        assertEquals(expected, span.attributes)
    }

    @Test
    fun testKnownMethods() = runTest {
        val methods = listOf("GET", "HEAD", "POST", "PUT", "DELETE", "CONNECT", "OPTIONS", "TRACE", "PATCH", "QUERY")
        val client = client(okEngine())
        methods.forEach { client.request("https://example.com") { method = HttpMethod(it) } }

        assertEquals(methods, spans.map { it.name })
        assertEquals(methods, spans.map { it.attributes[HTTP_REQUEST_METHOD] })
        assertTrue(spans.none { HTTP_REQUEST_METHOD_ORIGINAL in it.attributes })
    }

    @Test
    fun testUnknownMethod() = runTest {
        client(okEngine()).request("https://example.com") { method = HttpMethod("PURGE") }

        val span = spans.single()
        assertEquals("HTTP", span.name)
        assertEquals("_OTHER", span.attributes[HTTP_REQUEST_METHOD])
        assertEquals("PURGE", span.attributes[HTTP_REQUEST_METHOD_ORIGINAL])
    }

    @Test
    fun testMethodIsCaseSensitive() = runTest {
        client(okEngine()).request("https://example.com") { method = HttpMethod("get") }

        val span = spans.single()
        assertEquals("HTTP", span.name)
        assertEquals("_OTHER", span.attributes[HTTP_REQUEST_METHOD])
        assertEquals("get", span.attributes[HTTP_REQUEST_METHOD_ORIGINAL])
    }

    @Test
    fun testUrlCredentialsRedacted() = runTest {
        val client = client(okEngine())
        client.get("https://user:secret@example.com/a")
        client.get("https://token@example.com/b")

        assertEquals(
            listOf("https://REDACTED:REDACTED@example.com/a", "https://REDACTED:REDACTED@example.com/b"),
            spans.map { it.attributes[URL_FULL] },
        )
    }

    @Test
    fun testSensitiveQueryParametersRedacted() = runTest {
        val client = client(okEngine())
        client.get("https://example.com/a?color=blue&sig=abc&X-Amz-Signature=def&X-Goog-Signature=ghi#top")
        client.get("https://example.com/b?X-Amz-Credential=a&X-Amz-Security-Token=b&Sig=c")

        assertEquals(
            listOf(
                "https://example.com/a?color=blue&sig=REDACTED&X-Amz-Signature=REDACTED&X-Goog-Signature=REDACTED#top",
                "https://example.com/b?X-Amz-Credential=REDACTED&X-Amz-Security-Token=REDACTED&Sig=c",
            ),
            spans.map { it.attributes[URL_FULL] },
        )
    }

    @Test
    fun testServerAddressAndPort() = runTest {
        val client = client(okEngine())
        client.get("http://example.com/a")
        client.get("http://10.0.0.1:8080/b")

        val (default, explicit) = spans
        assertEquals("example.com", default.attributes[SERVER_ADDRESS])
        assertEquals(80L, default.attributes[SERVER_PORT])
        assertEquals("10.0.0.1", explicit.attributes[SERVER_ADDRESS])
        assertEquals(8080L, explicit.attributes[SERVER_PORT])
        assertEquals("http://10.0.0.1:8080/b", explicit.attributes[URL_FULL])
    }

    @Test
    fun testRedirectRecordsSpanPerRequest() = runTest {
        val engine = MockEngine { request ->
            if (request.url.encodedPath == "/old") {
                respond("", HttpStatusCode.Found, headersOf(HttpHeaders.Location, "/new"))
            } else {
                respond("ok", HttpStatusCode.OK)
            }
        }
        client(engine).get("https://example.com/old")

        val (redirect, final) = spans
        assertEquals("https://example.com/old", redirect.attributes[URL_FULL])
        assertEquals(302L, redirect.attributes[HTTP_RESPONSE_STATUS_CODE])
        assertEquals(StatusCode.UNSET, redirect.status.statusCode)
        assertEquals("https://example.com/new", final.attributes[URL_FULL])
        assertEquals(StatusCode.UNSET, final.status.statusCode)
    }

    @Test
    fun testErrorStatusCodes() = runTest {
        val statuses = listOf(HttpStatusCode.NotFound, HttpStatusCode.ServiceUnavailable)
        statuses.forEach { status -> client(MockEngine { respond("", status) }).get("https://example.com") }

        statuses.zip(spans).forEach { (status, span) ->
            assertEquals(StatusCode.ERROR, span.status.statusCode)
            assertEquals(null, span.status.description)
            assertEquals(status.value.toLong(), span.attributes[HTTP_RESPONSE_STATUS_CODE])
            assertEquals(status.value.toString(), span.attributes[ERROR_TYPE])
        }
    }

    @Test
    fun testErrorStatusCodeWithExpectSuccess() = runTest {
        val client = HttpClient(MockEngine { respond("", HttpStatusCode.NotFound) }) {
            expectSuccess = true
            install(openTelemetryKtorClientPlugin(openTelemetry()))
        }

        assertFailsWith<ClientRequestException> { client.get("https://example.com") }

        val span = spans.single()
        assertEquals(StatusCode.ERROR, span.status.statusCode)
        assertEquals(404L, span.attributes[HTTP_RESPONSE_STATUS_CODE])
        assertEquals("404", span.attributes[ERROR_TYPE])
    }

    @Test
    fun testRequestFail() = runTest {
        val client = client(MockEngine { throw BoomException() })

        assertFailsWith<BoomException> { client.get("http://example.com/x") }

        val span = spans.single()
        assertTrue(span.hasEnded)
        assertEquals(StatusCode.ERROR, span.status.statusCode)
        assertEquals(null, span.status.description)
        assertNotNull(span.attributes[ERROR_TYPE])
        assertEquals(BoomException().exceptionType(), span.attributes[ERROR_TYPE])
        assertFalse(HTTP_RESPONSE_STATUS_CODE in span.attributes)
    }

    @Test
    fun testRequestTimeout() = runTest {
        val engine = MockEngine {
            delay(60_000)
            respond("ok")
        }
        val client = HttpClient(engine) {
            install(HttpTimeout) { requestTimeoutMillis = 10 }
            install(openTelemetryKtorClientPlugin(openTelemetry()))
        }

        val exc = assertFailsWith<HttpRequestTimeoutException> { client.get("https://example.com") }

        val span = spans.single()
        assertEquals(StatusCode.ERROR, span.status.statusCode)
        assertEquals(exc.exceptionType(), span.attributes[ERROR_TYPE])
    }

    @Test
    fun testRequestCancelled() = runTest {
        val started = CompletableDeferred<Unit>()
        val client = client(
            MockEngine {
                started.complete(Unit)
                awaitCancellation()
            },
        )

        val job = launch { client.get("https://example.com") }
        started.await()
        job.cancelAndJoin()

        val span = spans.single()
        assertEquals(StatusCode.UNSET, span.status.statusCode)
        assertFalse(ERROR_TYPE in span.attributes)
    }

    private fun openTelemetry(): OpenTelemetry =
        createOpenTelemetry {
            tracerProvider { export { processor } }
        }

    private fun client(
        engine: MockEngine,
        openTelemetry: OpenTelemetry = openTelemetry(),
    ): HttpClient = HttpClient(engine) {
        install(openTelemetryKtorClientPlugin(openTelemetry))
    }

    private fun okEngine(): MockEngine = MockEngine { respond("ok", HttpStatusCode.OK) }
}

private class BoomException : RuntimeException("boom")
