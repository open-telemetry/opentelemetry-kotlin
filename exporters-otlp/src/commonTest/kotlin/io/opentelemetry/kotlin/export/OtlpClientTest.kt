package io.opentelemetry.kotlin.export

import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.toByteReadPacket
import io.ktor.client.plugins.HttpTimeoutConfig.Companion.INFINITE_TIMEOUT_MS
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.util.toMap
import io.ktor.utils.io.ByteReadChannel
import io.opentelemetry.kotlin.logging.export.toProtobufByteArray
import io.opentelemetry.kotlin.logging.model.FakeReadableLogRecord
import io.opentelemetry.kotlin.logging.model.ReadableLogRecord
import io.opentelemetry.kotlin.tracing.data.FakeSpanData
import io.opentelemetry.kotlin.tracing.data.SpanData
import io.opentelemetry.kotlin.tracing.export.toProtobufByteArray
import io.opentelemetry.proto.collector.logs.v1.ExportLogsPartialSuccess
import io.opentelemetry.proto.collector.logs.v1.ExportLogsServiceResponse
import io.opentelemetry.proto.collector.trace.v1.ExportTracePartialSuccess
import io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceResponse
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import kotlinx.io.readByteArray
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.time.Duration.Companion.milliseconds

internal class OtlpClientTest {

    private val requestTimeoutMs = 250L
    private val logRecords = listOf(FakeReadableLogRecord())
    private val spans = listOf(FakeSpanData())
    private val baseUrl = "http://localhost:1234"
    private val expectedUserAgent = "OTel-OTLP-Exporter-Kotlin/${BuildKonfig.VERSION}"

    private lateinit var client: OtlpClient
    private lateinit var server: MockEngine
    private lateinit var mockResponseStatus: HttpStatusCode
    private var mockResponseHeaders: Headers = Headers.Empty
    private var mockResponseBody: ByteArray = ByteArray(0)
    private var serverDelayMs: Long = 0

    @BeforeTest
    fun setUp() {
        server = MockEngine {
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
        client = OtlpClient(baseUrl, httpClient = httpClient)
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
                FakeReadableLogRecord(body = "a"),
                FakeReadableLogRecord(body = "b")
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
            mockResponseStatus = HttpStatusCode.GatewayTimeout,
            expectedResponse = OtlpResponse.ServerError(504, null),
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
            mockResponseStatus = HttpStatusCode.GatewayTimeout,
            expectedResponse = OtlpResponse.ServerError(504, null),
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
    fun testExportLog4xxDeserialization() = runTest {
        mockResponseStatus = HttpStatusCode.BadRequest
        mockResponseBody = logResponseBody(rejected = 0L, msg = "bad request")
        val response = client.exportLogs(logRecords)
        assertIs<OtlpResponse.ClientError>(response)
        assertEquals("bad request", response.errorMessage)
    }

    @Test
    fun testExportLog5xxDeserialization() = runTest {
        mockResponseStatus = HttpStatusCode.InternalServerError
        mockResponseBody = logResponseBody(rejected = 0L, msg = "internal error")
        val response = client.exportLogs(logRecords)
        assertIs<OtlpResponse.ServerError>(response)
        assertEquals("internal error", response.errorMessage)
    }

    @Test
    fun testExportTrace4xxDeserialization() = runTest {
        mockResponseStatus = HttpStatusCode.BadRequest
        mockResponseBody = traceResponseBody(rejected = 0L, msg = "bad request")
        val response = client.exportTraces(spans)
        assertIs<OtlpResponse.ClientError>(response)
        assertEquals("bad request", response.errorMessage)
    }

    @Test
    fun testExportTrace5xxDeserialization() = runTest {
        mockResponseStatus = HttpStatusCode.InternalServerError
        mockResponseBody = traceResponseBody(rejected = 0L, msg = "internal error")
        val response = client.exportTraces(spans)
        assertIs<OtlpResponse.ServerError>(response)
        assertEquals("internal error", response.errorMessage)
    }

    @Test
    fun testExportLogPartialSuccess() = runTest {
        mockResponseStatus = HttpStatusCode.OK
        mockResponseBody = logResponseBody(rejected = 2L, msg = "2 log records rejected")
        val response = client.exportLogs(logRecords)
        assertFalse(response is OtlpResponse.Success)
    }

    @Test
    fun testExportTracePartialSuccess() = runTest {
        mockResponseStatus = HttpStatusCode.OK
        mockResponseBody = traceResponseBody(rejected = 3L, msg = "3 spans rejected")
        val response = client.exportTraces(spans)
        assertFalse(response is OtlpResponse.Success)
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

    private fun logResponseBody(rejected: Long, msg: String): ByteArray =
        ExportLogsServiceResponse.ADAPTER.encode(
            ExportLogsServiceResponse(partial_success = ExportLogsPartialSuccess(rejected, msg))
        )

    private fun traceResponseBody(rejected: Long, msg: String): ByteArray =
        ExportTraceServiceResponse.ADAPTER.encode(
            ExportTraceServiceResponse(partial_success = ExportTracePartialSuccess(rejected, msg))
        )

    private suspend fun sendAndAssertLogRequest(
        telemetry: List<ReadableLogRecord>,
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

        val headers = request.headers.toMap().mapValues { it.value.joinToString() }
        assertEquals("gzip,deflate", headers["Accept-Encoding"])

        val bytes = request.body.toByteReadPacket().readByteArray()
        return bytes
    }

    private fun useRequestTimeout() {
        val httpClient = createDefaultHttpClient(requestTimeoutMs, server)
        client = OtlpClient(baseUrl, httpClient = httpClient)
    }
}
