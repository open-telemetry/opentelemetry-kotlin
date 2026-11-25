package io.opentelemetry.kotlin.export

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.error.FakeSdkErrorHandler
import io.opentelemetry.kotlin.error.NoopSdkErrorHandler
import io.opentelemetry.kotlin.error.SdkErrorHandler
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.coroutines.cancellation.CancellationException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration
import kotlin.time.Duration.Companion.microseconds
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalApi::class, ExperimentalCoroutinesApi::class)
internal class BatchTelemetryProcessorTest {

    @Test
    fun testSingleItemInBatch() = runTest {
        val exports = assertTelemetryBatched(
            telemetry = listOf(1, 2, 3),
            batchSize = 1
        )
        val expected = listOf(
            listOf(1),
            listOf(2),
            listOf(3),
        )
        assertEquals(expected, exports)
    }

    @Test
    fun testMultipleItemsInBatch() = runTest {
        val exports = assertTelemetryBatched(
            telemetry = listOf(1, 2, 3, 4, 5, 6, 7, 8)
        )
        val expected = listOf(
            listOf(1, 2, 3),
            listOf(4, 5, 6),
            listOf(7, 8),
        )
        assertEquals(expected, exports)
    }

    @Test
    fun testNoExportAfterShutdown() = runTest {
        val exports = mutableListOf<List<Int>>()
        val dispatcher = StandardTestDispatcher(testScheduler)
        val processor = BatchTelemetryProcessor(
            config = BatchTelemetryConfig(
                maxQueueSize = 100,
                maxExportBatchSize = 1,
                scheduleDelay = 1.milliseconds,
                exportTimeout = 1000.milliseconds,
                sdkErrorHandler = NoopSdkErrorHandler,
            ),
            dispatcher = dispatcher,
            exportAction = {
                exports.add(it)
                OperationResultCode.Success
            }
        )
        processor.processTelemetry(1)
        advanceTimeBy(10)
        processor.forceFlush()
        processor.shutdown()
        advanceUntilIdle()
        processor.processTelemetry(2)
        advanceTimeBy(10)
        advanceUntilIdle()
        assertEquals(1, exports.size)
    }

    @Test
    fun testShutdownReturnsSuccessOnSecondCall() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val processor = BatchTelemetryProcessor<Int>(
            config = BatchTelemetryConfig(
                maxQueueSize = 100,
                maxExportBatchSize = 1,
                scheduleDelay = 1.milliseconds,
                exportTimeout = 1000.milliseconds,
                sdkErrorHandler = NoopSdkErrorHandler,
            ),
            dispatcher = dispatcher,
            exportAction = { OperationResultCode.Success }
        )
        assertEquals(OperationResultCode.Success, processor.shutdown())
        assertEquals(OperationResultCode.Success, processor.shutdown())
    }

    @Test
    fun testForceFlushWorksAfterShutdown() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val processor = BatchTelemetryProcessor<Int>(
            config = BatchTelemetryConfig(
                maxQueueSize = 100,
                maxExportBatchSize = 1,
                scheduleDelay = 1.milliseconds,
                exportTimeout = 1000.milliseconds,
                sdkErrorHandler = NoopSdkErrorHandler,
            ),
            dispatcher = dispatcher,
            exportAction = { OperationResultCode.Success }
        )
        processor.shutdown()
        advanceUntilIdle()
        assertEquals(OperationResultCode.Success, processor.forceFlush())
    }

    @Test
    fun testQueueSaturation() = runTest {
        val exports = assertTelemetryBatched(
            telemetry = (0..1000).toList()
        )
        assertEquals((0..19).toList(), exports.flatten())
    }

    @Test
    fun testExportThrowsError() = runTest {
        val exports = assertTelemetryBatched(
            telemetry = listOf(1, 2, 3, 4, 5, 6, 7, 8),
            exportAction = {
                if (it.contains(5)) {
                    error("Simulating export error")
                }
            }
        )
        val expected = listOf(
            listOf(1, 2, 3),
            listOf(7, 8),
        )
        assertEquals(expected, exports)
    }

    @Test
    fun testExportTimeoutRespected() = runTest {
        val exports = assertTelemetryBatched(
            telemetry = listOf(1, 2, 3),
            batchSize = 1,
            exportTimeout = 1.milliseconds,
            exportAction = {
                delay(2)
            }
        )
        assertEquals(emptyList(), exports)
    }

    @Test
    fun testSubMillisecondExportTimeoutAllowsImmediateExport() = runTest {
        val exports = assertTelemetryBatched(
            telemetry = listOf(1),
            exportTimeout = 500.microseconds,
        )

        assertEquals(listOf(listOf(1)), exports)
    }

    @Test
    fun testUnlimitedExportTimeoutsExceedDefaultTimeout() = runTest {
        for (exportTimeout in listOf(Duration.ZERO, Duration.INFINITE)) {
            val exports = mutableListOf<List<Int>>()
            val processor = createProcessor(
                exportTimeout = exportTimeout,
                forceFlushTimeoutMs = BatchTelemetryDefaults.EXPORT_TIMEOUT_MS + 1000,
            ) {
                delay(BatchTelemetryDefaults.EXPORT_TIMEOUT_MS.milliseconds + 1.milliseconds)
                exports += it
                OperationResultCode.Success
            }
            processor.processTelemetry(1)

            assertEquals(OperationResultCode.Success, processor.forceFlush())
            assertEquals(listOf(listOf(1)), exports)
            processor.shutdown()
        }
    }

    @Test
    fun testFullBatchExportedEarlyAndShutdownExportsRemainder() = runTest {
        val exports = mutableListOf<List<Int>>()
        val processor = createProcessor {
            exports.add(it)
            OperationResultCode.Success
        }
        listOf(1, 2, 3, 4).forEach(processor::processTelemetry)
        advanceTimeBy(10)
        assertEquals(listOf(listOf(1, 2, 3)), exports)

        assertEquals(OperationResultCode.Success, processor.shutdown())
        assertEquals(listOf(listOf(1, 2, 3), listOf(4)), exports)
    }

    @Test
    fun testPartialBatchExportedAfterScheduleDelay() = runTest {
        val exports = mutableListOf<List<Int>>()
        val processor = createProcessor(scheduleDelayMs = 10) {
            exports.add(it)
            OperationResultCode.Success
        }
        processor.processTelemetry(1)
        advanceTimeBy(6)
        processor.processTelemetry(2)
        advanceTimeBy(3)
        assertEquals(emptyList(), exports)

        // the delay runs from the first item in the batch, so later items don't extend it
        advanceTimeBy(2)
        assertEquals(listOf(listOf(1, 2)), exports)

        processor.shutdown()
    }

    @Test
    fun testForceFlushExportsThenFlushes() = runTest {
        val events = mutableListOf<String>()
        val processor = createProcessor(
            flushAction = {
                events += "flush"
                OperationResultCode.Success
            },
        ) {
            events += "export $it"
            OperationResultCode.Failure
        }
        listOf(1, 2).forEach(processor::processTelemetry)
        assertEquals(OperationResultCode.Failure, processor.forceFlush())
        assertEquals(listOf("export [1, 2]", "flush"), events)

        processor.shutdown()
    }

    @Test
    fun testExportIsNeverConcurrent() = runTest {
        var inFlight = 0
        var maxInFlight = 0
        val exported = mutableListOf<Int>()
        val processor = createProcessor(scheduleDelayMs = 1, batchSize = 2) {
            maxInFlight = maxOf(maxInFlight, ++inFlight)
            delay(5)
            exported += it
            inFlight--
            OperationResultCode.Success
        }
        (0 until 50).map { value ->
            processor.processTelemetry(value)
            async { processor.forceFlush() }
        }.awaitAll()
        processor.shutdown()

        assertEquals(1, maxInFlight)
        assertEquals((0 until 50).toList(), exported)
    }

    @Test
    fun testForceFlushTimeoutDoesNotBlockWorker() = runTest {
        val exports = mutableListOf<List<Int>>()
        val processor = createProcessor(forceFlushTimeoutMs = 10, exportTimeout = 50.milliseconds) {
            if (it == listOf(1)) {
                delay(100)
            }
            exports.add(it)
            OperationResultCode.Success
        }
        processor.processTelemetry(1)
        assertEquals(OperationResultCode.Timeout, processor.forceFlush())

        // the worker recovers once the stuck export hits its timeout
        advanceTimeBy(100)
        processor.processTelemetry(2)
        assertEquals(OperationResultCode.Success, processor.forceFlush())
        assertEquals(listOf(listOf(2)), exports)

        processor.shutdown()
    }

    @Test
    fun testForceFlushReportsExportTimeout() = runTest {
        val processor = createProcessor(exportTimeoutMs = 10) {
            delay(100)
            OperationResultCode.Success
        }
        processor.processTelemetry(1)
        assertEquals(OperationResultCode.Timeout, processor.forceFlush())

        processor.shutdown()
    }

    @Test
    fun testForceFlushReportsExporterFlushTimeoutOverExportFailure() = runTest {
        val processor = createProcessor(flushAction = { OperationResultCode.Timeout }) {
            OperationResultCode.Failure
        }
        processor.processTelemetry(1)
        assertEquals(OperationResultCode.Timeout, processor.forceFlush())

        processor.shutdown()
    }

    @Test
    fun testAndPreservesTimeout() {
        val timeout = OperationResultCode.Timeout
        listOf(OperationResultCode.Success, OperationResultCode.Failure, timeout).forEach { other ->
            assertEquals(timeout, timeout and other)
            assertEquals(timeout, other and timeout)
        }
        assertEquals(OperationResultCode.Success, OperationResultCode.Success and OperationResultCode.Success)
        assertEquals(OperationResultCode.Failure, OperationResultCode.Success and OperationResultCode.Failure)
        assertEquals(OperationResultCode.Failure, OperationResultCode.Failure and OperationResultCode.Success)
    }

    @Test
    fun testExporterCancellationDoesNotStopWorker() = runTest {
        val errorHandler = FakeSdkErrorHandler()
        val exports = mutableListOf<List<Int>>()
        val processor = createProcessor(sdkErrorHandler = errorHandler) {
            exports.add(it)
            if (it == listOf(1)) {
                throw CancellationException("exporter cancelled")
            }
            OperationResultCode.Success
        }
        processor.processTelemetry(1)
        assertEquals(OperationResultCode.Failure, processor.forceFlush())

        processor.processTelemetry(2)
        assertEquals(OperationResultCode.Success, processor.forceFlush())
        assertEquals(listOf(listOf(1), listOf(2)), exports)
        assertEquals(1, errorHandler.userCodeErrors.size)

        processor.shutdown()
    }

    @Test
    fun testExporterFlushCancellationDoesNotStopWorker() = runTest {
        val errorHandler = FakeSdkErrorHandler()
        var flushes = 0
        val exports = mutableListOf<List<Int>>()
        val processor = createProcessor(
            sdkErrorHandler = errorHandler,
            flushAction = {
                if (flushes++ == 0) {
                    throw CancellationException("exporter cancelled")
                }
                OperationResultCode.Success
            },
        ) {
            exports.add(it)
            OperationResultCode.Success
        }
        assertEquals(OperationResultCode.Failure, processor.forceFlush())

        processor.processTelemetry(1)
        assertEquals(OperationResultCode.Success, processor.forceFlush())
        assertEquals(listOf(listOf(1)), exports)
        assertEquals(1, errorHandler.userCodeErrors.size)

        processor.shutdown()
    }

    private fun TestScope.createProcessor(
        scheduleDelayMs: Long = 10_000,
        batchSize: Int = 3,
        exportTimeout: Duration = 1000.milliseconds,
        forceFlushTimeoutMs: Long = 1000,
        sdkErrorHandler: SdkErrorHandler = NoopSdkErrorHandler,
        flushAction: suspend () -> OperationResultCode = { OperationResultCode.Success },
        exportAction: suspend (List<Int>) -> OperationResultCode,
    ) = BatchTelemetryProcessor(
        config = BatchTelemetryConfig(
            maxQueueSize = 100,
            maxExportBatchSize = batchSize,
            scheduleDelay = scheduleDelayMs.milliseconds,
            exportTimeout = exportTimeout,
            forceFlushTimeoutMs = forceFlushTimeoutMs,
            sdkErrorHandler = sdkErrorHandler,
        ),
        dispatcher = StandardTestDispatcher(testScheduler),
        flushAction = flushAction,
        exportAction = exportAction,
    )

    private suspend fun <T> TestScope.assertTelemetryBatched(
        telemetry: List<T>,
        batchSize: Int = 3,
        exportTimeout: Duration = 1000.milliseconds,
        maxQueueSize: Int = 20,
        exportAction: suspend (telemetry: List<T>) -> Unit = {},
    ): List<List<T>> {
        val exports = mutableListOf<List<T>>()
        val dispatcher = StandardTestDispatcher(testScheduler)
        val processor = BatchTelemetryProcessor(
            config = BatchTelemetryConfig(
                maxQueueSize = maxQueueSize,
                maxExportBatchSize = batchSize,
                scheduleDelay = 1.milliseconds,
                exportTimeout = exportTimeout,
                sdkErrorHandler = NoopSdkErrorHandler,
            ),
            dispatcher = dispatcher,
            exportAction = {
                exportAction(it)
                exports.add(it)
                OperationResultCode.Success
            }
        )
        telemetry.forEach {
            processor.processTelemetry(it)
        }

        // allow JS event loop to process
        advanceTimeBy(10)
        processor.forceFlush()
        processor.shutdown()

        // wait for all coroutines to finish running
        advanceUntilIdle()
        return exports
    }
}
