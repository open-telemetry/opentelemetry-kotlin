package io.opentelemetry.kotlin.export

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.error.NoopSdkErrorHandler
import io.opentelemetry.kotlin.error.SdkErrorHandler
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import kotlin.coroutines.CoroutineContext
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.TimeSource

@OptIn(ExperimentalApi::class, ExperimentalCoroutinesApi::class)
internal class TelemetryExporterTest {

    private val dispatcher: TestDispatcher = StandardTestDispatcher()

    private fun <T> initTelemetryExporter(
        initialDelayMs: Long = 100,
        maxAttemptIntervalMs: Long = 1000,
        maxAttempts: Int = 3,
        sdkErrorHandler: SdkErrorHandler = NoopSdkErrorHandler,
        coroutineContext: CoroutineContext,
        timeSource: TimeSource.WithComparableMarks,
        random: Random = Random.Default,
        exportAction: suspend (telemetry: List<T>) -> OtlpResponse = { OtlpResponse.Success },
    ) = TelemetryExporter(
        initialDelayMs = initialDelayMs,
        maxAttemptIntervalMs = maxAttemptIntervalMs,
        maxAttempts = maxAttempts,
        sdkErrorHandler = sdkErrorHandler,
        coroutineContext = coroutineContext,
        timeSource = timeSource,
        random = random,
        exportAction = exportAction
    )

    @Test
    fun testExportReturnsFailureAfterShutdown() = runTest(dispatcher) {
        val exporter =
            initTelemetryExporter<String>(coroutineContext = coroutineContext, timeSource = testScheduler.timeSource) {
                OtlpResponse.Success
            }
        assertEquals(OperationResultCode.Success, exporter.shutdown())
        assertEquals(OperationResultCode.Failure, exporter.export(listOf("data")))
    }

    @Test
    fun testShutdownReturnsSuccessOnSecondCall() = runTest(dispatcher) {
        val exporter =
            initTelemetryExporter<String>(coroutineContext = coroutineContext, timeSource = testScheduler.timeSource) {
                OtlpResponse.Success
            }
        assertEquals(OperationResultCode.Success, exporter.shutdown())
        assertEquals(OperationResultCode.Success, exporter.shutdown())
    }

    @Test
    fun testForceFlushWorksAfterShutdown() = runTest(dispatcher) {
        val exporter =
            initTelemetryExporter<String>(coroutineContext = coroutineContext, timeSource = testScheduler.timeSource) {
                OtlpResponse.Success
            }
        assertEquals(OperationResultCode.Success, exporter.shutdown())
        assertEquals(OperationResultCode.Success, exporter.forceFlush())
    }

    @Test
    fun testExportSucceedsBeforeShutdown() = runTest(dispatcher) {
        val exporter =
            initTelemetryExporter<String>(coroutineContext = coroutineContext, timeSource = testScheduler.timeSource) {
                OtlpResponse.Success
            }
        assertEquals(OperationResultCode.Success, exporter.export(listOf("data")))
        exporter.shutdown()
    }

    @Test
    fun testClientErrorIsNotRetried() = runTest(dispatcher) {
        var attempts = 0
        val exporter =
            initTelemetryExporter<String>(coroutineContext = coroutineContext, timeSource = testScheduler.timeSource) {
                attempts++
                OtlpResponse.ClientError(400, null)
            }
        exporter.export(listOf("data"))
        advanceTimeBy(5_000)
        assertEquals(1, attempts)
        exporter.shutdown()
    }

    @Test
    fun testSuccessIsNotRetried() = runTest(dispatcher) {
        var attempts = 0
        val exporter =
            initTelemetryExporter<String>(coroutineContext = coroutineContext, timeSource = testScheduler.timeSource) {
                attempts++
                OtlpResponse.Success
            }
        exporter.export(listOf("data"))
        advanceTimeBy(5_000)
        assertEquals(1, attempts)
        exporter.shutdown()
    }

    @Test
    fun testRetryableErrorExponentialBackoff() = runTest(dispatcher) {
        val timestamps = mutableListOf<Long>()
        val maxAttempts = 4
        val initialDelayMs = 100L
        val maxIntervalMs = 1000L
        val exporter = initTelemetryExporter<String>(
            initialDelayMs = initialDelayMs,
            maxAttemptIntervalMs = maxIntervalMs,
            maxAttempts = maxAttempts,
            sdkErrorHandler = NoopSdkErrorHandler,
            coroutineContext = coroutineContext,
            timeSource = testScheduler.timeSource,
            random = Random(0),
        ) {
            timestamps += testScheduler.currentTime
            OtlpResponse.RetryableError(503, retryAfterMs = null, errorMessage = null)
        }
        exporter.export(listOf("data"))
        advanceTimeBy(5_000)
        exporter.shutdown()

        assertEquals(maxAttempts, timestamps.size)

        val pollingIntervalMs = 100L
        val maxJitterMs = 100L
        var expectedBackoffMs = initialDelayMs
        timestamps.zipWithNext { previous, next -> next - previous }.forEach { delta ->
            val upperExclusive = expectedBackoffMs + pollingIntervalMs + maxJitterMs
            assertTrue(
                delta in expectedBackoffMs..<upperExclusive,
                "backoff delta $delta outside [$expectedBackoffMs, $upperExclusive)",
            )
            expectedBackoffMs = (expectedBackoffMs * 2).coerceAtMost(maxIntervalMs)
        }
    }

    @Test
    fun testHonorsRetryAfter() = runTest(dispatcher) {
        val retryAfterMs = 5000L
        val timestamps = mutableListOf<Long>()
        var attempts = 0
        val exporter = initTelemetryExporter<String>(
            initialDelayMs = 100,
            maxAttemptIntervalMs = 1000,
            maxAttempts = 3,
            random = Random(0),
            sdkErrorHandler = NoopSdkErrorHandler,
            coroutineContext = coroutineContext,
            timeSource = testScheduler.timeSource
        ) {
            timestamps += testScheduler.currentTime
            if (attempts++ == 0) {
                OtlpResponse.RetryableError(429, retryAfterMs = retryAfterMs, errorMessage = null)
            } else {
                OtlpResponse.Success
            }
        }
        exporter.export(listOf("data"))
        advanceTimeBy(10_000)
        exporter.shutdown()

        assertEquals(2, timestamps.size)
        val pollingIntervalMs = 100L
        val maxJitterMs = 100L
        val delta = timestamps[1] - timestamps[0]
        val upperExclusive = retryAfterMs + pollingIntervalMs + maxJitterMs
        assertTrue(
            delta in retryAfterMs..<upperExclusive,
            "retry delta $delta outside [$retryAfterMs, $upperExclusive)",
        )
    }

    fun testExportDoesNotPropagateExportActionFailure() = runTest(dispatcher) {
        val throwingExporter = initTelemetryExporter<String>(
            initialDelayMs = 1,
            maxAttemptIntervalMs = 1,
            maxAttempts = 1,
            sdkErrorHandler = NoopSdkErrorHandler,
            coroutineContext = coroutineContext,
            timeSource = testScheduler.timeSource
        ) { error("network unreachable") }
        // The failure occurs on a background coroutine whose scope has a CoroutineExceptionHandler,
        // so it must not propagate to the caller nor crash the process.
        assertEquals(OperationResultCode.Success, throwingExporter.export(listOf("data")))
    }
}
