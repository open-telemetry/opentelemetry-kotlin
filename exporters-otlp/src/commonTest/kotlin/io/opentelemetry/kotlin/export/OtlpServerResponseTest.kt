package io.opentelemetry.kotlin.export

import io.ktor.http.HttpStatusCode
import io.opentelemetry.kotlin.error.FakeSdkErrorHandler
import io.opentelemetry.kotlin.export.FakeOtlpServer.Companion.failure
import io.opentelemetry.kotlin.export.FakeOtlpServer.Companion.logPartialSuccess
import io.opentelemetry.kotlin.export.FakeOtlpServer.Companion.proxyErrorPage
import io.opentelemetry.kotlin.export.FakeOtlpServer.Companion.redirect
import io.opentelemetry.kotlin.export.FakeOtlpServer.Companion.tracePartialSuccess
import io.opentelemetry.kotlin.export.FakeOtlpServer.Companion.traceSuccess
import io.opentelemetry.kotlin.logging.data.FakeLogRecordData
import io.opentelemetry.kotlin.tracing.data.FakeSpanData
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Exercises [OtlpClient] and [TelemetryExporter] together against a [FakeOtlpServer], verifying
 * that each response a real server can send is classified and retried as the OTLP/HTTP spec
 * requires: https://opentelemetry.io/docs/specs/otlp/#otlphttp-response
 */
@OptIn(ExperimentalCoroutinesApi::class)
internal class OtlpServerResponseTest {

    private val errorHandler = FakeSdkErrorHandler()

    @Test
    fun testFullSuccessIsSentOnce() = runTest {
        val (server, responses) = export(traceSuccess())
        assertEquals(1, server.requestCount)
        assertIs<OtlpResponse.Success>(responses.single())
    }

    @Test
    fun testGzipEncodedSuccessIsSentOnce() = runTest {
        val (server, responses) = export(traceSuccess(gzip = true))
        assertEquals(1, server.requestCount)
        assertIs<OtlpResponse.Success>(responses.single())
    }

    @Test
    fun testOtherSuccessStatusCodesAreSentOnce() = runTest {
        listOf(HttpStatusCode.Created, HttpStatusCode.Accepted, HttpStatusCode.NoContent).forEach { status ->
            val (server, responses) = export(traceSuccess(status))
            assertEquals(1, server.requestCount, "status $status")
            assertIs<OtlpResponse.Success>(responses.single(), "status $status")
        }
    }

    @Test
    fun testUndecodableSuccessBodyIsSentOnce() = runTest {
        val (server, responses) = export(FakeOtlpServer.Respond(HttpStatusCode.OK, "OK".encodeToByteArray()))
        assertEquals(1, server.requestCount)
        assertIs<OtlpResponse.Success>(responses.single())
    }

    @Test
    fun testPartialSuccessIsNotRetried() = runTest {
        val (server, responses) = export(
            tracePartialSuccess(rejected = 2, message = "2 spans had an invalid trace_id")
        )
        assertEquals(1, server.requestCount)
        val response = assertIs<OtlpResponse.PartialSuccess>(responses.single())
        assertEquals(2, response.rejectedCount)
        assertEquals("2 spans had an invalid trace_id", response.errorMessage)
    }

    @Test
    fun testPartialSuccessWarningIsNotRetried() = runTest {
        val (server, responses) = export(
            tracePartialSuccess(rejected = 0, message = "attribute keys should use snake_case")
        )
        assertEquals(1, server.requestCount)
        val response = assertIs<OtlpResponse.PartialSuccess>(responses.single())
        assertEquals(0, response.rejectedCount)
        assertEquals("attribute keys should use snake_case", response.errorMessage)
    }

    @Test
    fun testGzipEncodedPartialSuccessIsNotRetried() = runTest {
        val (server, responses) = export(tracePartialSuccess(rejected = 1, message = "span rejected", gzip = true))
        assertEquals(1, server.requestCount)
        assertEquals(1, assertIs<OtlpResponse.PartialSuccess>(responses.single()).rejectedCount)
    }

    @Test
    fun testLogPartialSuccessIsNotRetried() = runTest {
        val (server, responses) = export(
            logPartialSuccess(rejected = 3, message = "3 log records rejected"),
            signal = Signal.LOGS,
        )
        assertEquals(1, server.requestCount)
        val response = assertIs<OtlpResponse.PartialSuccess>(responses.single())
        assertEquals(3, response.rejectedCount)
        assertEquals("3 log records rejected", response.errorMessage)
    }

    @Test
    fun testBadRequestIsNotRetried() = runTest {
        val (server, responses) = export(
            failure(HttpStatusCode.BadRequest, "invalid trace_id", grpcCode = 3, withBadRequestDetails = true)
        )
        assertEquals(1, server.requestCount)
        val response = assertIs<OtlpResponse.ClientError>(responses.single())
        assertEquals(400, response.statusCode)
        assertEquals("invalid trace_id", response.errorMessage)
    }

    @Test
    fun testLogBadRequestIsNotRetried() = runTest {
        val (server, responses) = export(
            failure(HttpStatusCode.BadRequest, "invalid severity_number", grpcCode = 3),
            signal = Signal.LOGS,
        )
        assertEquals(1, server.requestCount)
        assertEquals("invalid severity_number", assertIs<OtlpResponse.ClientError>(responses.single()).errorMessage)
    }

    @Test
    fun testOtherClientErrorsAreNotRetried() = runTest {
        listOf(
            HttpStatusCode.Unauthorized,
            HttpStatusCode.Forbidden,
            HttpStatusCode.NotFound,
            HttpStatusCode.MethodNotAllowed,
            HttpStatusCode.RequestTimeout,
            HttpStatusCode.PayloadTooLarge,
            HttpStatusCode.UnsupportedMediaType,
        ).forEach { status ->
            val (server, responses) = export(failure(status))
            assertEquals(1, server.requestCount, "status $status")
            val response = assertIs<OtlpResponse.ClientError>(responses.single(), "status $status")
            assertEquals(status.description, response.errorMessage)
        }
    }

    @Test
    fun testNonRetryableServerErrorsAreNotRetried() = runTest {
        listOf(
            HttpStatusCode.InternalServerError,
            HttpStatusCode.NotImplemented,
            HttpStatusCode.VersionNotSupported,
            HttpStatusCode.InsufficientStorage,
        ).forEach { status ->
            val (server, responses) = export(failure(status, grpcCode = 13))
            assertEquals(1, server.requestCount, "status $status")
            val response = assertIs<OtlpResponse.ServerError>(responses.single(), "status $status")
            assertEquals(status.description, response.errorMessage)
        }
    }

    @Test
    fun testNonRetryableProxyErrorPageIsNotRetried() = runTest {
        val (server, responses) = export(proxyErrorPage(HttpStatusCode.InternalServerError))
        assertEquals(1, server.requestCount)
        assertNull(assertIs<OtlpResponse.ServerError>(responses.single()).errorMessage)
    }

    @Test
    fun testRetryableStatusCodesAreRetriedUntilSuccess() = runTest {
        listOf(
            HttpStatusCode.TooManyRequests,
            HttpStatusCode.BadGateway,
            HttpStatusCode.ServiceUnavailable,
            HttpStatusCode.GatewayTimeout,
        ).forEach { status ->
            val (server, responses) = export(failure(status, grpcCode = 14), traceSuccess())
            assertEquals(2, server.requestCount, "status $status")
            val first = assertIs<OtlpResponse.RetryableError>(responses.first(), "status $status")
            assertEquals(status.value, first.statusCode)
            assertEquals(status.description, first.errorMessage)
            assertIs<OtlpResponse.Success>(responses.last(), "status $status")
        }
    }

    @Test
    fun testRetryableProxyErrorPageIsRetried() = runTest {
        val (server, responses) = export(proxyErrorPage(HttpStatusCode.BadGateway), traceSuccess())
        assertEquals(2, server.requestCount)
        assertNull(assertIs<OtlpResponse.RetryableError>(responses.first()).errorMessage)
    }

    @Test
    fun testRetryableFailureThenPartialSuccessStopsRetrying() = runTest {
        val (server, responses) = export(
            failure(HttpStatusCode.ServiceUnavailable),
            tracePartialSuccess(rejected = 1, message = "span rejected"),
            traceSuccess(),
        )
        assertEquals(2, server.requestCount)
        assertIs<OtlpResponse.PartialSuccess>(responses.last())
    }

    @Test
    fun testRetryableFailureThenNonRetryableFailureStopsRetrying() = runTest {
        val (server, responses) = export(
            failure(HttpStatusCode.ServiceUnavailable),
            failure(HttpStatusCode.BadRequest),
            traceSuccess(),
        )
        assertEquals(2, server.requestCount)
        assertIs<OtlpResponse.ClientError>(responses.last())
    }

    @Test
    fun testPersistentRetryableFailureStopsAtMaxAttempts() = runTest {
        val (server, responses) = export(failure(HttpStatusCode.ServiceUnavailable))
        assertEquals(MAX_ATTEMPTS, server.requestCount)
        assertTrue(responses.all { it is OtlpResponse.RetryableError })
    }

    @Test
    fun testRetryableFailureUsesJitteredExponentialBackoff() = runTest {
        val (server, _) = export(failure(HttpStatusCode.ServiceUnavailable))
        var base = INITIAL_DELAY_MS
        server.intervals().forEach { interval ->
            assertTrue(interval in (base / 2)..base, "interval $interval outside [${base / 2}, $base]")
            base = (base * 2).coerceAtMost(MAX_INTERVAL_MS)
        }
    }

    @Test
    fun testRetryAfterSecondsIsCappedAtMaxInterval() = runTest {
        listOf(HttpStatusCode.TooManyRequests, HttpStatusCode.ServiceUnavailable).forEach { status ->
            val (server, responses) = export(failure(status, retryAfter = "30"), traceSuccess())
            assertEquals(2, server.requestCount, "status $status")
            assertEquals(30_000L, assertIs<OtlpResponse.RetryableError>(responses.first()).retryAfterMs)
            assertEquals(listOf(MAX_INTERVAL_MS), server.intervals(), "status $status")
        }
    }

    @Test
    fun testZeroRetryAfterRetriesImmediately() = runTest {
        val (server, _) = export(failure(HttpStatusCode.TooManyRequests, retryAfter = "0"), traceSuccess())
        assertEquals(listOf(0L), server.intervals())
    }

    @Test
    fun testUnsupportedRetryAfterFallsBackToBackoff() = runTest {
        listOf("-1", "soon").forEach { retryAfter ->
            val (server, responses) = export(
                failure(HttpStatusCode.TooManyRequests, retryAfter = retryAfter),
                traceSuccess(),
            )
            assertNull(assertIs<OtlpResponse.RetryableError>(responses.first()).retryAfterMs, retryAfter)
            val interval = server.intervals().single()
            assertTrue(interval in (INITIAL_DELAY_MS / 2)..INITIAL_DELAY_MS, "Retry-After '$retryAfter': $interval")
        }
    }

    @Test
    fun testUnfollowedRedirectsAreNotRetried() = runTest {
        listOf(
            HttpStatusCode.MovedPermanently,
            HttpStatusCode.Found,
            HttpStatusCode.TemporaryRedirect,
            HttpStatusCode.PermanentRedirect,
        ).forEach { status ->
            val (server, responses) = export(redirect(status), traceSuccess())
            assertEquals(1, server.requestCount, "status $status")
            assertEquals(status.value, assertIs<OtlpResponse.UnexpectedStatus>(responses.single()).statusCode)
            assertEquals("/v1/traces", server.requestUrls.single().encodedPath, "status $status")
        }
    }

    @Test
    fun testDisconnectIsRetriedUntilSuccess() = runTest {
        val (server, responses) = export(FakeOtlpServer.Disconnect, FakeOtlpServer.Disconnect, traceSuccess())
        assertEquals(3, server.requestCount)
        assertIs<OtlpResponse.Unknown>(responses.first())
        assertIs<OtlpResponse.Success>(responses.last())
        assertEquals(2, errorHandler.userCodeErrors.size)
    }

    @Test
    fun testPersistentDisconnectStopsAtMaxAttempts() = runTest {
        val (server, responses) = export(FakeOtlpServer.Disconnect)
        assertEquals(MAX_ATTEMPTS, server.requestCount)
        assertTrue(responses.all { it is OtlpResponse.Unknown })
    }

    @Test
    fun testRequestTimeoutIsRetriedUntilSuccess() = runTest {
        val (server, responses) = export(
            FakeOtlpServer.Slow(REQUEST_TIMEOUT_MS * 2, traceSuccess()),
            traceSuccess(),
        )
        assertEquals(2, server.requestCount)
        assertIs<OtlpResponse.Unknown>(responses.first())
        assertIs<OtlpResponse.Success>(responses.last())
    }

    @Test
    fun testOversizedResponseIsNotRetried() = runTest {
        listOf(HttpStatusCode.OK, HttpStatusCode.ServiceUnavailable).forEach { status ->
            val oversized = FakeOtlpServer.Respond(status, ByteArray(MAX_RESPONSE_BYTES + 1))
            val (server, responses) = export(oversized, traceSuccess())
            assertEquals(1, server.requestCount, "status $status")
            assertIs<OtlpResponse.ResponseTooLarge>(responses.single(), "status $status")
        }
        assertEquals(2, errorHandler.userCodeErrors.size)
    }

    @Test
    fun testGzipEncodedOversizedResponseIsNotRetried() = runTest {
        val (server, responses) = export(
            FakeOtlpServer.Respond(HttpStatusCode.OK, ByteArray(MAX_RESPONSE_BYTES + 1), gzip = true),
            traceSuccess(),
        )
        assertEquals(1, server.requestCount)
        assertIs<OtlpResponse.ResponseTooLarge>(responses.single())
    }

    private enum class Signal { TRACES, LOGS }

    private data class ExportResult(val server: FakeOtlpServer, val responses: List<OtlpResponse>)

    /**
     * Exports one batch through a [TelemetryExporter] backed by a real [OtlpClient] talking to a
     * [FakeOtlpServer] that sends [replies] in order, and returns once all attempts have finished.
     */
    private suspend fun TestScope.export(
        vararg replies: FakeOtlpServer.Reply,
        signal: Signal = Signal.TRACES,
    ): ExportResult {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val server = FakeOtlpServer(testScheduler, dispatcher, *replies)
        val client = OtlpClient(
            baseUrl = "http://localhost:4318",
            httpClient = createDefaultHttpClient(REQUEST_TIMEOUT_MS, server.engine),
            sdkErrorHandler = errorHandler,
        )
        val responses = mutableListOf<OtlpResponse>()
        val exporter = TelemetryExporter<Any>(
            initialDelayMs = INITIAL_DELAY_MS,
            maxAttemptIntervalMs = MAX_INTERVAL_MS,
            maxAttempts = MAX_ATTEMPTS,
            sdkErrorHandler = errorHandler,
            coroutineContext = dispatcher,
        ) {
            when (signal) {
                Signal.TRACES -> client.exportTraces(listOf(FakeSpanData()))
                Signal.LOGS -> client.exportLogs(listOf(FakeLogRecordData()))
            }.also { responses += it }
        }
        assertEquals(OperationResultCode.Success, exporter.export(listOf(Unit)))
        advanceUntilIdle()
        assertEquals(server.requestCount, responses.size)
        exporter.shutdown()
        return ExportResult(server, responses)
    }

    private companion object {
        const val INITIAL_DELAY_MS = 1000L
        const val MAX_INTERVAL_MS = 4000L
        const val MAX_ATTEMPTS = 5
        const val REQUEST_TIMEOUT_MS = 10_000L
        const val MAX_RESPONSE_BYTES = 4 * 1024 * 1024
    }
}
