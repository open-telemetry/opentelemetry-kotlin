package io.opentelemetry.kotlin.export

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.error.NoopSdkErrorHandler
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalApi::class, ExperimentalCoroutinesApi::class)
internal class TelemetryExporterTest {

    private fun TestScope.initTelemetryExporter(
        initialDelayMs: Long = 100,
        maxAttemptIntervalMs: Long = 1000,
        maxAttempts: Int = 3,
        random: Random = Random.Default,
        exportAction: suspend (List<String>) -> OtlpResponse = { OtlpResponse.Success },
    ) = TelemetryExporter(
        initialDelayMs = initialDelayMs,
        maxAttemptIntervalMs = maxAttemptIntervalMs,
        maxAttempts = maxAttempts,
        sdkErrorHandler = NoopSdkErrorHandler,
        coroutineContext = StandardTestDispatcher(testScheduler),
        timeSource = testScheduler.timeSource,
        random = random,
        exportAction = exportAction,
    )

    @Test
    fun testExportReturnsFailureAfterShutdown() = runTest {
        initTelemetryExporter().withShutdown { exporter ->
            assertEquals(OperationResultCode.Success, exporter.shutdown())
            assertEquals(OperationResultCode.Failure, exporter.export(listOf("data")))
        }
    }

    @Test
    fun testShutdownReturnsSuccessOnSecondCall() = runTest {
        initTelemetryExporter().withShutdown { exporter ->
            assertEquals(OperationResultCode.Success, exporter.shutdown())
            assertEquals(OperationResultCode.Success, exporter.shutdown())
        }
    }

    @Test
    fun testForceFlushWorksAfterShutdown() = runTest {
        initTelemetryExporter().withShutdown { exporter ->
            assertEquals(OperationResultCode.Success, exporter.shutdown())
            assertEquals(OperationResultCode.Success, exporter.forceFlush())
        }
    }

    @Test
    fun testExportSucceedsBeforeShutdown() = runTest {
        initTelemetryExporter().withShutdown { exporter ->
            assertEquals(OperationResultCode.Success, exporter.export(listOf("data")))
        }
    }

    @Test
    fun testClientErrorIsNotRetried() = runTest {
        var attempts = 0
        val exporter = initTelemetryExporter {
            attempts++
            OtlpResponse.ClientError(400, null)
        }
        exporter.withShutdown {
            exporter.export(listOf("data"))
            advanceTimeBy(5_000)
            assertEquals(1, attempts)
        }
    }

    @Test
    fun testTerminalResponsesAreNotRetried() = runTest {
        listOf(
            OtlpResponse.ServerError(500, null),
            OtlpResponse.UnexpectedStatus(301),
            OtlpResponse.ResponseTooLarge(503),
            OtlpResponse.PartialSuccess(1, null),
        ).forEach { response ->
            var attempts = 0
            val exporter = initTelemetryExporter {
                attempts++
                response
            }
            exporter.withShutdown {
                exporter.export(listOf("data"))
                advanceTimeBy(5_000)
                assertEquals(1, attempts, "$response")
            }
        }
    }

    @Test
    fun testUnknownIsRetried() = runTest {
        var attempts = 0
        val exporter = initTelemetryExporter {
            attempts++
            OtlpResponse.Unknown
        }
        exporter.withShutdown {
            exporter.export(listOf("data"))
            advanceTimeBy(5_000)
            assertEquals(3, attempts)
        }
    }

    @Test
    fun testSuccessIsNotRetried() = runTest {
        var attempts = 0
        val exporter = initTelemetryExporter {
            attempts++
            OtlpResponse.Success
        }
        exporter.withShutdown {
            exporter.export(listOf("data"))
            advanceTimeBy(5_000)
            assertEquals(1, attempts)
        }
    }

    @Test
    fun testRetryableErrorExponentialBackoff() = runTest {
        val timestamps = mutableListOf<Long>()
        val maxAttempts = 4
        val initialDelayMs = 100L
        val maxIntervalMs = 1000L
        val exporter = initTelemetryExporter(
            initialDelayMs = initialDelayMs,
            maxAttemptIntervalMs = maxIntervalMs,
            maxAttempts = maxAttempts,
            random = Random(0),
        ) {
            timestamps += testScheduler.currentTime
            OtlpResponse.RetryableError(503, retryAfterMs = null, errorMessage = null)
        }
        exporter.withShutdown {
            exporter.export(listOf("data"))
            advanceTimeBy(5_000)

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
    }

    @Test
    fun testHonorsRetryAfter() = runTest {
        val retryAfterMs = 5000L
        val timestamps = recordRetryAfterAttempts(retryAfterMs, maxAttemptIntervalMs = 10_000)
        assertEquals(2, timestamps.size)
        assertTrue(timestamps[1] - timestamps[0] in retryAfterMs..<(retryAfterMs + 200))
    }

    @Test
    fun testRetryAfterIsCappedAtMaxAttemptInterval() = runTest {
        val maxAttemptIntervalMs = 1000L
        val timestamps = recordRetryAfterAttempts(Long.MAX_VALUE, maxAttemptIntervalMs)
        assertEquals(2, timestamps.size)
        assertTrue(timestamps[1] - timestamps[0] in maxAttemptIntervalMs..<(maxAttemptIntervalMs + 200))
    }

    private suspend fun TestScope.recordRetryAfterAttempts(
        retryAfterMs: Long,
        maxAttemptIntervalMs: Long,
    ): List<Long> {
        val timestamps = mutableListOf<Long>()
        var attempts = 0
        val exporter = initTelemetryExporter(
            maxAttemptIntervalMs = maxAttemptIntervalMs,
            random = Random(0),
        ) {
            timestamps += testScheduler.currentTime
            if (attempts++ == 0) {
                OtlpResponse.RetryableError(429, retryAfterMs = retryAfterMs, errorMessage = null)
            } else {
                OtlpResponse.Success
            }
        }
        return exporter.withShutdown {
            exporter.export(listOf("data"))
            advanceTimeBy(10_000)
            timestamps
        }
    }

    @Test
    fun testExportFailsImmediatelyWhenQueueIsFull() = runTest {
        val gate = CompletableDeferred<Unit>()
        val exporter = initTelemetryExporter {
            gate.await()
            OtlpResponse.Success
        }
        exporter.withShutdown {
            repeat(MAX_QUEUE_ENTRIES) {
                assertEquals(OperationResultCode.Success, exporter.export(listOf("data")))
            }
            val start = currentTime
            assertEquals(OperationResultCode.Failure, exporter.export(listOf("overflow")))
            assertEquals(start, currentTime)

            // Capacity is restored once queued entries complete.
            gate.complete(Unit)
            testScheduler.advanceUntilIdle()
            assertEquals(OperationResultCode.Success, exporter.export(listOf("data")))
        }
    }

    @Test
    fun testShutdownSkipsDrainWhenBlockedPastDeadline() = runTest {
        var attempts = 0
        val exporter = initTelemetryExporter(maxAttemptIntervalMs = 60_000, maxAttempts = 5) {
            attempts++
            OtlpResponse.RetryableError(503, retryAfterMs = 30_000, errorMessage = null)
        }
        exporter.export(listOf("data"))
        advanceTimeBy(100)

        val start = testScheduler.currentTime
        assertEquals(OperationResultCode.Failure, exporter.shutdown())
        assertEquals(start, testScheduler.currentTime)
        assertEquals(1, attempts)
    }

    @Test
    fun testShutdownDrainsWhenBackoffEndsBeforeDeadline() = runTest {
        var attempts = 0
        val exporter = initTelemetryExporter(maxAttemptIntervalMs = 60_000, random = Random(0)) {
            if (attempts++ == 0) {
                OtlpResponse.RetryableError(503, retryAfterMs = 1000, errorMessage = null)
            } else {
                OtlpResponse.Success
            }
        }
        exporter.export(listOf("data"))
        advanceTimeBy(100)

        assertEquals(OperationResultCode.Success, exporter.shutdown())
        assertEquals(2, attempts)
    }

    @Test
    fun testShutdownIsBoundedByCallerDeadline() = runTest {
        var exportCancelled = false
        val exporter = initTelemetryExporter {
            try {
                awaitCancellation()
            } finally {
                exportCancelled = true
            }
        }
        exporter.export(listOf("data"))
        advanceTimeBy(100)

        val start = testScheduler.currentTime
        val result = withTimeoutOrNull(1000) { exporter.shutdown() }
        assertNull(result)
        assertEquals(start + 1000, testScheduler.currentTime)
        assertTrue(exportCancelled)
    }

    @Test
    fun testShutdownWaitsForInFlightEntryWhileBlocked() = runTest {
        var delivered = false
        val exporter = initTelemetryExporter(maxAttemptIntervalMs = 60_000, maxAttempts = 5) { batch ->
            if (batch == listOf("blocked")) {
                delay(50)
                OtlpResponse.RetryableError(503, retryAfterMs = 30_000, errorMessage = null)
            } else {
                delay(2000)
                delivered = true
                OtlpResponse.Success
            }
        }
        exporter.export(listOf("blocked"))
        exporter.export(listOf("in-flight"))
        advanceTimeBy(100)

        val start = testScheduler.currentTime
        assertEquals(OperationResultCode.Failure, exporter.shutdown())
        assertTrue(delivered)
        assertEquals(start + 5000, testScheduler.currentTime)
    }

    @Test
    fun testShutdownSucceedsWithEmptyQueueWhileBlocked() = runTest {
        val exporter = initTelemetryExporter(maxAttemptIntervalMs = 60_000, maxAttempts = 1) {
            OtlpResponse.RetryableError(503, retryAfterMs = 30_000, errorMessage = null)
        }
        exporter.export(listOf("data"))
        advanceTimeBy(100)

        assertEquals(OperationResultCode.Success, exporter.shutdown())
    }

    @Test
    fun testShutdownTimesOutWhenExportHangs() = runTest {
        val exporter = initTelemetryExporter { awaitCancellation() }
        exporter.export(listOf("data"))
        advanceTimeBy(100)

        val start = testScheduler.currentTime
        assertEquals(OperationResultCode.Failure, exporter.shutdown())
        assertEquals(start + 5000, testScheduler.currentTime)
    }

    @Test
    fun testExportDoesNotPropagateExportActionFailure() = runTest {
        val throwingExporter = initTelemetryExporter(
            initialDelayMs = 1,
            maxAttemptIntervalMs = 1,
            maxAttempts = 1,
            exportAction = { error("network unreachable") },
        )
        throwingExporter.withShutdown {
            // The failure occurs on a background coroutine whose scope has a CoroutineExceptionHandler,
            // so it must not propagate to the caller nor crash the process.
            assertEquals(OperationResultCode.Success, throwingExporter.export(listOf("data")))
        }
    }
}
