package io.opentelemetry.kotlin.export

import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.toByteArray
import io.ktor.client.plugins.HttpTimeoutConfig.Companion.INFINITE_TIMEOUT_MS
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.http.toHttpDate
import io.ktor.util.GZipEncoder
import io.ktor.util.date.GMTDate
import io.ktor.util.toMap
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.toByteArray
import io.opentelemetry.kotlin.error.FakeSdkErrorHandler
import io.opentelemetry.kotlin.error.NoopSdkErrorHandler
import io.opentelemetry.kotlin.logging.data.FakeLogRecordData
import io.opentelemetry.kotlin.logging.data.LogRecordData
import io.opentelemetry.kotlin.logging.export.toProtobufByteArray
import io.opentelemetry.kotlin.tracing.data.FakeSpanData
import io.opentelemetry.kotlin.tracing.data.SpanData
import io.opentelemetry.kotlin.tracing.export.toProtobufByteArray
import io.opentelemetry.proto.collector.logs.v1.ExportLogsPartialSuccess
import io.opentelemetry.proto.collector.logs.v1.ExportLogsServiceResponse
import io.opentelemetry.proto.collector.trace.v1.ExportTracePartialSuccess
import io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceResponse
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds

internal class OtlpClientTest {

    private val requestTimeoutMs = 250L
    private val logRecords = listOf(FakeLogRecordData())
    private val spans = listOf(FakeSpanData())
    private val baseUrl = "http://localhost:1234"
    private val expectedUserAgent = "OTel-OTLP-Exporter-Kotlin/${BuildKonfig.VERSION}"

    private lateinit var client: OtlpClient
    private var errorHandler: FakeSdkErrorHandler = FakeSdkErrorHandler()
    private lateinit var server: MockEngine
    private lateinit var mockResponseStatus: HttpStatusCode
    private var mockResponseHeaders: Headers = Headers.Empty
    private var mockResponseBody: ByteArray = ByteArray(0)
    private var serverDelayMs: Long = 0
    private var serverThrows: Boolean = false
    private var compressedRequestBody: ByteArray = ByteArray(0)

    @BeforeTest
    fun setUp() {
        errorHandler = FakeSdkErrorHandler()
        server = MockEngine {
            if (serverThrows) {
                error("network unreachable")
            }
            // gzip compression is lazy and tied to the request context, so capture the body here;
            // reading it later fails after the request completes.
            compressedRequestBody = it.body.toByteArray()
            if (serverDelayMs > 0) {
                delay(serverDelayMs.milliseconds)
            }
            respond(
                content = ByteReadChannel(mockResponseBody),
                status = mockResponseStatus,
                headers = mockResponseHeaders,
            )
        }
        val httpClient = createDefaultHttpClient(INFINITE_TIMEOUT_MS, server)
        client = OtlpClient(baseUrl, httpClient = httpClient, sdkErrorHandler = errorHandler)
    }

    @Test
    fun testExportSingleLogSuccess() = runTest {
        sendAndAssertLogRequest(
            telemetry = logRecords,
            mockResponseStatus = HttpStatusCode.OK,
            expectedResponse = OtlpResponse.Success,
        )
    }

    @Test
    fun testExportMultiLogSuccess() = runTest {
        sendAndAssertLogRequest(
            telemetry = listOf(
                FakeLogRecordData(body = "a"),
                FakeLogRecordData(body = "b")
            ),
            mockResponseStatus = HttpStatusCode.OK,
            expectedResponse = OtlpResponse.Success,
        )
    }

    @Test
    fun testExportLogClientError() = runTest {
        sendAndAssertLogRequest(
            telemetry = logRecords,
            mockResponseStatus = HttpStatusCode.BadRequest,
            expectedResponse = OtlpResponse.ClientError(400, null)
        )
    }

    @Test
    fun testExportLogServerError() = runTest {
        sendAndAssertLogRequest(
            telemetry = logRecords,
            mockResponseStatus = HttpStatusCode.InternalServerError,
            expectedResponse = OtlpResponse.ServerError(500, null),
        )
    }

    @Test
    fun testExportSingleTraceSuccess() = runTest {
        sendAndAssertTraceRequest(
            telemetry = spans,
            mockResponseStatus = HttpStatusCode.OK,
            expectedResponse = OtlpResponse.Success,
        )
    }

    @Test
    fun testExportMultiTraceSuccess() = runTest {
        sendAndAssertTraceRequest(
            telemetry = listOf(
                FakeSpanData(name = "a"),
                FakeSpanData(name = "b"),
            ),
            mockResponseStatus = HttpStatusCode.OK,
            expectedResponse = OtlpResponse.Success,
        )
    }

    @Test
    fun testExportTraceClientError() = runTest {
        sendAndAssertTraceRequest(
            telemetry = spans,
            mockResponseStatus = HttpStatusCode.BadRequest,
            expectedResponse = OtlpResponse.ClientError(400, null)
        )
    }

    @Test
    fun testExportTraceServerError() = runTest {
        sendAndAssertTraceRequest(
            telemetry = spans,
            mockResponseStatus = HttpStatusCode.InternalServerError,
            expectedResponse = OtlpResponse.ServerError(500, null),
        )
    }

    @Test
    fun testExportLogClientTimeout() = runTest {
        serverDelayMs = 10_000
        useRequestTimeout()
        sendAndAssertLogRequest(
            telemetry = logRecords,
            mockResponseStatus = HttpStatusCode.OK,
            expectedResponse = OtlpResponse.Unknown,
        )
    }

    @Test
    fun testExportTraceClientTimeout() = runTest {
        serverDelayMs = 10_000
        useRequestTimeout()
        sendAndAssertTraceRequest(
            telemetry = spans,
            mockResponseStatus = HttpStatusCode.OK,
            expectedResponse = OtlpResponse.Unknown,
        )
    }

    @Test
    fun testExportLogNetworkFailureDoesNotThrow() = runTest {
        serverThrows = true
        val response = client.exportLogs(logRecords)
        assertIs<OtlpResponse.Unknown>(response)
        assertEquals(1, errorHandler.userCodeErrors.size)
        assertEquals("OTLP export failed", errorHandler.userCodeErrors.single().message)
    }

    @Test
    fun testExportTraceNetworkFailureDoesNotThrow() = runTest {
        serverThrows = true
        val response = client.exportTraces(spans)
        assertIs<OtlpResponse.Unknown>(response)
        assertEquals(1, errorHandler.userCodeErrors.size)
        assertEquals("OTLP export failed", errorHandler.userCodeErrors.single().message)
    }

    @Test
    fun testExportLogsSetsUserAgentHeader() = runTest {
        mockResponseStatus = HttpStatusCode.OK
        client.exportLogs(logRecords)

        val request = server.requestHistory.single()
        val headers = request.headers.toMap().mapValues { it.value.joinToString() }
        val userAgent = headers["User-Agent"]
        assertEquals(expectedUserAgent, userAgent)
    }

    @Test
    fun testExportTracesSetsUserAgentHeader() = runTest {
        mockResponseStatus = HttpStatusCode.OK
        client.exportTraces(spans)

        val request = server.requestHistory.single()
        val headers = request.headers.toMap().mapValues { it.value.joinToString() }
        val userAgent = headers["User-Agent"]
        assertEquals(expectedUserAgent, userAgent)
    }

    @Test
    fun testHeadersAreProvidedForEachSignalRequest() = runTest {
        mockResponseStatus = HttpStatusCode.OK
        var token = "first"
        client = createOtlpHttpClient(errorHandler) {
            httpClient = createDefaultHttpClient(INFINITE_TIMEOUT_MS, server)
            headers = { mapOf(HttpHeaders.Authorization to "Bearer $token") }
        }

        assertEquals(OtlpResponse.Success, client.exportTraces(spans))
        token = "second"
        assertEquals(OtlpResponse.Success, client.exportLogs(logRecords))

        assertEquals("Bearer first", server.requestHistory[0].headers[HttpHeaders.Authorization])
        assertEquals("Bearer second", server.requestHistory[1].headers[HttpHeaders.Authorization])
        assertEquals(expectedUserAgent, server.requestHistory[0].headers[HttpHeaders.UserAgent])
    }

    @Test
    fun testHeaderProviderFailureIsReportedWithoutSending() = runTest {
        client = createOtlpHttpClient(errorHandler) {
            httpClient = createDefaultHttpClient(INFINITE_TIMEOUT_MS, server)
            headers = { error("token refresh failed") }
        }

        assertEquals(OtlpResponse.Unknown, client.exportTraces(spans))
        assertEquals(1, errorHandler.userCodeErrors.size)
        assertEquals(0, server.requestHistory.size)
    }

    @Test
    fun testCreateOtlpHttpClientInvalidValuesFallBackToDefault() {
        val fakeHandler = FakeSdkErrorHandler()
        val createdClient = createOtlpHttpClient(fakeHandler) {
            endpoint = ""
            timeoutMs = -1
        }
        assertEquals(DEFAULT_OTLP_HTTP_ENDPOINT, createdClient.baseUrl)
        assertEquals(2, fakeHandler.apiMisuses.size)
    }

    @Test
    fun testExportLogRetryableError() = runTest {
        mockResponseStatus = HttpStatusCode.TooManyRequests
        val response = client.exportLogs(logRecords)
        assertIs<OtlpResponse.RetryableError>(response)
        assertEquals(429, response.statusCode)
        assertNull(response.retryAfterMs)
    }

    @Test
    fun testExportTraceRetryableError() = runTest {
        mockResponseStatus = HttpStatusCode.TooManyRequests
        val response = client.exportTraces(spans)
        assertIs<OtlpResponse.RetryableError>(response)
        assertEquals(429, response.statusCode)
        assertNull(response.retryAfterMs)
    }

    @Test
    fun testExportLogRetryableErrorHonoursRetryAfter() = runTest {
        mockResponseStatus = HttpStatusCode.TooManyRequests
        mockResponseHeaders = headersOf(HttpHeaders.RetryAfter, "5")
        val response = client.exportLogs(logRecords)
        assertIs<OtlpResponse.RetryableError>(response)
        assertEquals(5000L, response.retryAfterMs)
    }

    @Test
    fun testExportTraceRetryableErrorHonoursRetryAfter() = runTest {
        mockResponseStatus = HttpStatusCode.ServiceUnavailable
        mockResponseHeaders = headersOf(HttpHeaders.RetryAfter, "12")
        val response = client.exportTraces(spans)
        assertIs<OtlpResponse.RetryableError>(response)
        assertEquals(12_000L, response.retryAfterMs)
    }

    @Test
    fun testRetryAfterHttpDateInFuture() = runTest {
        val nowSeconds = GMTDate().timestamp / 1000
        val retryAt = GMTDate((nowSeconds + 30) * 1000)
        val retryAfterMs = exportTracesWithRetryAfter(retryAt.toHttpDate())
        assertNotNull(retryAfterMs)
        assertTrue(retryAfterMs in 25_000L..30_000L, "unexpected retryAfterMs $retryAfterMs")
    }

    @Test
    fun testRetryAfterHttpDateInPastIsZero() = runTest {
        assertEquals(0L, exportTracesWithRetryAfter("Wed, 21 Oct 2015 07:28:00 GMT"))
    }

    @Test
    fun testRetryAfterLargeSecondsDoesNotOverflow() = runTest {
        val retryAfterMs = exportTracesWithRetryAfter(Long.MAX_VALUE.toString())
        assertNotNull(retryAfterMs)
        assertTrue(retryAfterMs > 0)
    }

    @Test
    fun testRetryAfterMalformedIsIgnored() = runTest {
        listOf("soon", "-5", "1.5", "", "99999999999999999999").forEach {
            assertNull(exportTracesWithRetryAfter(it), "expected null for '$it'")
        }
    }

    private suspend fun exportTracesWithRetryAfter(value: String): Long? {
        mockResponseStatus = HttpStatusCode.TooManyRequests
        mockResponseHeaders = headersOf(HttpHeaders.RetryAfter, value)
        val response = client.exportTraces(spans)
        assertIs<OtlpResponse.RetryableError>(response)
        return response.retryAfterMs
    }

    @Test
    fun testExportLog4xxDeserialization() = runTest {
        mockResponseStatus = HttpStatusCode.BadRequest
        mockResponseBody = statusBody("bad request")
        val response = client.exportLogs(logRecords)
        assertIs<OtlpResponse.ClientError>(response)
        assertEquals("bad request", response.errorMessage)
    }

    @Test
    fun testExportLog5xxDeserialization() = runTest {
        mockResponseStatus = HttpStatusCode.InternalServerError
        mockResponseBody = statusBody("internal error")
        val response = client.exportLogs(logRecords)
        assertIs<OtlpResponse.ServerError>(response)
        assertEquals("internal error", response.errorMessage)
    }

    @Test
    fun testExportTrace4xxDeserialization() = runTest {
        mockResponseStatus = HttpStatusCode.BadRequest
        mockResponseBody = statusBody("bad request")
        val response = client.exportTraces(spans)
        assertIs<OtlpResponse.ClientError>(response)
        assertEquals("bad request", response.errorMessage)
    }

    @Test
    fun testExportTrace5xxDeserialization() = runTest {
        mockResponseStatus = HttpStatusCode.InternalServerError
        mockResponseBody = statusBody("internal error")
        val response = client.exportTraces(spans)
        assertIs<OtlpResponse.ServerError>(response)
        assertEquals("internal error", response.errorMessage)
    }

    @Test
    fun testExportLogPartialSuccess() = runTest {
        mockResponseStatus = HttpStatusCode.OK
        mockResponseBody = logResponseBody(rejected = 2L, msg = "2 log records rejected")
        val response = client.exportLogs(logRecords)
        assertIs<OtlpResponse.PartialSuccess>(response)
        assertEquals(2L, response.rejectedCount)
        assertEquals("2 log records rejected", response.errorMessage)
    }

    @Test
    fun testExportTracePartialSuccess() = runTest {
        mockResponseStatus = HttpStatusCode.OK
        mockResponseBody = traceResponseBody(rejected = 3L, msg = "3 spans rejected")
        val response = client.exportTraces(spans)
        assertIs<OtlpResponse.PartialSuccess>(response)
        assertEquals(3L, response.rejectedCount)
        assertEquals("3 spans rejected", response.errorMessage)
    }

    @Test
    fun testExportLogUnexpectedHttpStatus() = runTest {
        mockResponseStatus = HttpStatusCode.MovedPermanently
        val response = client.exportLogs(logRecords)
        assertIs<OtlpResponse.UnexpectedStatus>(response)
        assertEquals(301, response.statusCode)
    }

    @Test
    fun testExportTraceUnexpectedHttpStatus() = runTest {
        mockResponseStatus = HttpStatusCode.MovedPermanently
        val response = client.exportTraces(spans)
        assertIs<OtlpResponse.UnexpectedStatus>(response)
        assertEquals(301, response.statusCode)
    }

    @Test
    fun testExportLogBadGatewayIsRetryable() = runTest {
        mockResponseStatus = HttpStatusCode.BadGateway
        val response = client.exportLogs(logRecords)
        assertIs<OtlpResponse.RetryableError>(response)
        assertEquals(502, response.statusCode)
    }

    @Test
    fun testExportTraceBadGatewayIsRetryable() = runTest {
        mockResponseStatus = HttpStatusCode.BadGateway
        val response = client.exportTraces(spans)
        assertIs<OtlpResponse.RetryableError>(response)
        assertEquals(502, response.statusCode)
    }

    @Test
    fun testExportNon200SuccessStatusIsSuccess() = runTest {
        listOf(HttpStatusCode.Created, HttpStatusCode.Accepted, HttpStatusCode.NoContent).forEach { status ->
            mockResponseStatus = status
            assertEquals(OtlpResponse.Success, client.exportLogs(logRecords))
            assertEquals(OtlpResponse.Success, client.exportTraces(spans))
        }
    }

    @Test
    fun testExportTraceAcceptedPartialSuccess() = runTest {
        mockResponseStatus = HttpStatusCode.Accepted
        mockResponseBody = traceResponseBody(rejected = 2L, msg = "2 spans rejected")
        val response = client.exportTraces(spans)
        assertIs<OtlpResponse.PartialSuccess>(response)
        assertEquals(2L, response.rejectedCount)
    }

    @Test
    fun testExportRetryableErrorDeserialization() = runTest {
        mockResponseStatus = HttpStatusCode.ServiceUnavailable
        mockResponseBody = statusBody("overloaded")
        val response = client.exportTraces(spans)
        assertIs<OtlpResponse.RetryableError>(response)
        assertEquals("overloaded", response.errorMessage)
    }

    @Test
    fun testExportOversizedResponseIsNotRetryable() = runTest {
        listOf(HttpStatusCode.OK, HttpStatusCode.ServiceUnavailable).forEach { status ->
            mockResponseStatus = status
            mockResponseBody = ByteArray(4 * 1024 * 1024 + 1)
            val response = client.exportLogs(logRecords)
            assertIs<OtlpResponse.ResponseTooLarge>(response)
            assertEquals(status.value, response.statusCode)
        }
        assertEquals(2, errorHandler.userCodeErrors.size)
    }

    @Test
    fun testExportResponseAtSizeLimitIsAccepted() = runTest {
        mockResponseStatus = HttpStatusCode.OK
        mockResponseBody = ByteArray(4 * 1024 * 1024)
        assertIs<OtlpResponse.Success>(client.exportLogs(logRecords))
        assertEquals(0, errorHandler.userCodeErrors.size)
    }

    @Test
    fun testExportLog200EmptyBodyIsSuccess() = runTest {
        mockResponseStatus = HttpStatusCode.OK
        mockResponseBody = ByteArray(0)
        assertEquals(OtlpResponse.Success, client.exportLogs(logRecords))
    }

    @Test
    fun testExportLogMalformedErrorBody() = runTest {
        mockResponseStatus = HttpStatusCode.BadRequest
        mockResponseBody = byteArrayOf(0xFF.toByte(), 0xFE.toByte(), 0x00, 0x42)
        val response = client.exportLogs(logRecords)
        assertEquals(400, response.statusCode)
    }

    @Test
    fun testExportTraceMalformedErrorBody() = runTest {
        mockResponseStatus = HttpStatusCode.BadRequest
        mockResponseBody = byteArrayOf(0xFF.toByte(), 0xFE.toByte(), 0x00, 0x42)
        val response = client.exportTraces(spans)
        assertEquals(400, response.statusCode)
    }

    /**
     * Encodes a google.rpc.Status with `code = 3` and the given message (< 128 bytes).
     */
    private fun statusBody(msg: String): ByteArray {
        val message = msg.encodeToByteArray()
        return byteArrayOf(0x08, 0x03, 0x12, message.size.toByte()) + message
    }

    private fun logResponseBody(rejected: Long, msg: String): ByteArray =
        ExportLogsServiceResponse.ADAPTER.encode(
            ExportLogsServiceResponse(partial_success = ExportLogsPartialSuccess(rejected, msg))
        )

    private fun traceResponseBody(rejected: Long, msg: String): ByteArray =
        ExportTraceServiceResponse.ADAPTER.encode(
            ExportTraceServiceResponse(partial_success = ExportTracePartialSuccess(rejected, msg))
        )

    private suspend fun sendAndAssertLogRequest(
        telemetry: List<LogRecordData>,
        mockResponseStatus: HttpStatusCode,
        expectedResponse: OtlpResponse
    ) {
        val bytes = sendAndAssertTelemetry(
            mockResponseStatus,
            expectedResponse,
            OtlpEndpoint.Logs
        ) {
            client.exportLogs(telemetry)
        } ?: return
        assertContentEquals(telemetry.toProtobufByteArray(), bytes)
    }

    private suspend fun sendAndAssertTraceRequest(
        telemetry: List<SpanData>,
        mockResponseStatus: HttpStatusCode,
        expectedResponse: OtlpResponse
    ) {
        val bytes = sendAndAssertTelemetry(
            mockResponseStatus,
            expectedResponse,
            OtlpEndpoint.Traces
        ) {
            client.exportTraces(telemetry)
        } ?: return
        assertContentEquals(telemetry.toProtobufByteArray(), bytes)
    }

    private suspend fun sendAndAssertTelemetry(
        mockResponseStatus: HttpStatusCode,
        expectedResponse: OtlpResponse,
        endpoint: OtlpEndpoint,
        exportAction: suspend () -> OtlpResponse,
    ): ByteArray? {
        this.mockResponseStatus = mockResponseStatus
        val response = exportAction()
        assertEquals(expectedResponse.statusCode, response.statusCode)

        if (expectedResponse is OtlpResponse.Unknown) {
            return null
        }

        val request = server.requestHistory.single()
        assertEquals(HttpMethod.Post, request.method)
        assertEquals("$baseUrl/${endpoint.path}", request.url.toString())

        val contentType = checkNotNull(request.body.contentType)
        assertEquals("application/x-protobuf", contentType.toString())

        assertEquals("gzip,deflate", request.headers[HttpHeaders.AcceptEncoding])

        assertEquals("gzip", request.body.headers[HttpHeaders.ContentEncoding])

        val uncompressedBytes = GZipEncoder.decode(ByteReadChannel(compressedRequestBody))
            .toByteArray()
        return uncompressedBytes
    }

    private fun useRequestTimeout() {
        val httpClient = createDefaultHttpClient(requestTimeoutMs, server)
        client = OtlpClient(baseUrl, httpClient = httpClient, sdkErrorHandler = NoopSdkErrorHandler)
    }
}
