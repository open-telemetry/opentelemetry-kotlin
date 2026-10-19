package io.opentelemetry.kotlin.export

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.error.NoopSdkErrorHandler
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
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
            advanceUntilIdle()
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
                advanceUntilIdle()
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
            advanceUntilIdle()
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
            advanceUntilIdle()
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
    }

    @Test
    fun testHonorsRetryAfter() = runTest {
        val retryAfterMs = 5000L
        val timestamps = recordRetryAfterAttempts(retryAfterMs, maxAttemptIntervalMs = 10_000)
        assertEquals(2, timestamps.size)
        assertEquals(retryAfterMs, timestamps[1] - timestamps[0])
    }

    @Test
    fun testRetryAfterIsCappedAtMaxAttemptInterval() = runTest {
        val maxAttemptIntervalMs = 1000L
        val timestamps = recordRetryAfterAttempts(Long.MAX_VALUE, maxAttemptIntervalMs)
        assertEquals(2, timestamps.size)
        assertEquals(maxAttemptIntervalMs, timestamps[1] - timestamps[0])
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
            advanceUntilIdle()
            timestamps
        }
    }

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
