package io.opentelemetry.kotlin.export

import io.opentelemetry.kotlin.ExperimentalApi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.random.Random
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalApi::class, ExperimentalCoroutinesApi::class)
internal class TelemetryExporterTest {

    private lateinit var exporter: TelemetryExporter<String>

    @BeforeTest
    fun setup() {
        exporter = TelemetryExporter(
            initialDelayMs = 100,
            maxAttemptIntervalMs = 1000,
            maxAttempts = 3,
            exportAction = { OtlpResponse.Success }
        )
    }

    @Test
    fun testExportReturnsFailureAfterShutdown() = runTest {
        assertEquals(OperationResultCode.Success, exporter.shutdown())
        assertEquals(OperationResultCode.Failure, exporter.export(listOf("data")))
    }

    @Test
    fun testShutdownReturnsSuccessOnSecondCall() = runTest {
        assertEquals(OperationResultCode.Success, exporter.shutdown())
        assertEquals(OperationResultCode.Success, exporter.shutdown())
    }

    @Test
    fun testForceFlushWorksAfterShutdown() = runTest {
        assertEquals(OperationResultCode.Success, exporter.shutdown())
        assertEquals(OperationResultCode.Success, exporter.forceFlush())
    }

    @Test
    fun testExportSucceedsBeforeShutdown() {
        assertEquals(OperationResultCode.Success, exporter.export(listOf("data")))
    }

    @Test
    fun testClientErrorIsNotRetried() = runTest {
        var attempts = 0
        val exporter = TelemetryExporter<String>(
            initialDelayMs = 100,
            maxAttemptIntervalMs = 1000,
            maxAttempts = 3,
            coroutineContext = StandardTestDispatcher(testScheduler),
        ) {
            attempts++
            OtlpResponse.ClientError(400, null)
        }
        exporter.export(listOf("data"))
        advanceUntilIdle()
        assertEquals(1, attempts)
    }

    @Test
    fun testSuccessIsNotRetried() = runTest {
        var attempts = 0
        val exporter = TelemetryExporter<String>(
            initialDelayMs = 100,
            maxAttemptIntervalMs = 1000,
            maxAttempts = 3,
            coroutineContext = StandardTestDispatcher(testScheduler),
        ) {
            attempts++
            OtlpResponse.Success
        }
        exporter.export(listOf("data"))
        advanceUntilIdle()
        assertEquals(1, attempts)
    }

    @Test
    fun testRetryableErrorExponentialBackoff() = runTest {
        val timestamps = mutableListOf<Long>()
        val maxAttempts = 4
        val initialDelayMs = 100L
        val maxIntervalMs = 1000L
        val exporter = TelemetryExporter<String>(
            initialDelayMs = initialDelayMs,
            maxAttemptIntervalMs = maxIntervalMs,
            maxAttempts = maxAttempts,
            coroutineContext = StandardTestDispatcher(testScheduler),
            random = Random(0),
        ) {
            timestamps += testScheduler.currentTime
            OtlpResponse.RetryableError(503, retryAfterMs = null, errorMessage = null)
        }
        exporter.export(listOf("data"))
        advanceUntilIdle()

        assertEquals(maxAttempts, timestamps.size)

        var base = initialDelayMs
        timestamps.zipWithNext { a, b -> b - a }.forEach { delta ->
            val cappedBase = base.coerceAtMost(maxIntervalMs)
            assertTrue(
                delta in (cappedBase / 2)..cappedBase,
                "backoff delta $delta outside [${cappedBase / 2}, $cappedBase]",
            )
            base = (base * 2).coerceAtMost(maxIntervalMs)
        }
    }

    @Test
    fun testHonorsRetryAfter() = runTest {
        val retryAfterMs = 5000L
        val timestamps = mutableListOf<Long>()
        var attempts = 0
        val exporter = TelemetryExporter<String>(
            initialDelayMs = 100,
            maxAttemptIntervalMs = 1000,
            maxAttempts = 3,
            coroutineContext = StandardTestDispatcher(testScheduler),
            random = Random(0),
        ) {
            timestamps += testScheduler.currentTime
            if (attempts++ == 0) {
                OtlpResponse.RetryableError(429, retryAfterMs = retryAfterMs, errorMessage = null)
            } else {
                OtlpResponse.Success
            }
        }
        exporter.export(listOf("data"))
        advanceUntilIdle()

        assertEquals(2, timestamps.size)
        assertEquals(retryAfterMs, timestamps[1] - timestamps[0])
    }
}
