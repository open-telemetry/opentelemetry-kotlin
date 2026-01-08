package io.opentelemetry.kotlin.export

import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.toByteReadPacket
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.util.toMap
import io.ktor.utils.io.ByteReadChannel
import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.logging.export.toProtobufByteArray
import io.opentelemetry.kotlin.logging.model.FakeReadableLogRecord
import io.opentelemetry.kotlin.logging.model.ReadableLogRecord
import io.opentelemetry.kotlin.tracing.data.FakeSpanData
import io.opentelemetry.kotlin.tracing.data.SpanData
import io.opentelemetry.kotlin.tracing.export.toProtobufByteArray
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import kotlinx.io.readByteArray
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

@OptIn(ExperimentalApi::class)
internal class OtlpClientTest {

    private val logRecords = listOf(FakeReadableLogRecord())
    private val spans = listOf(FakeSpanData())
    private val baseUrl = "http://localhost:1234"

    private lateinit var client: OtlpClient
    private lateinit var server: MockEngine
    private lateinit var mockResponseStatus: HttpStatusCode
    private var serverDelayMs: Long = 0

    @BeforeTest
    fun setUp() {
        server = MockEngine {
            if (serverDelayMs > 0) {
                delay(serverDelayMs)
            }
            respond(
                content = ByteReadChannel(""),
                status = mockResponseStatus
            )
        }
        val httpClient = createDefaultHttpClient(250, server)
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
        serverDelayMs = 10000
        sendAndAssertLogRequest(
            telemetry = logRecords,
            mockResponseStatus = HttpStatusCode.OK,
            expectedResponse = OtlpResponse.Unknown,
        )
    }

    @Test
    fun testExportTraceClientTimeout() = runTest {
        serverDelayMs = 10000
        sendAndAssertTraceRequest(
            telemetry = spans,
            mockResponseStatus = HttpStatusCode.OK,
            expectedResponse = OtlpResponse.Unknown,
        )
    }

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
}
