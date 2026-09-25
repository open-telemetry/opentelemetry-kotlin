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
                scheduleDelayMs = 1,
                exportTimeoutMs = 1000,
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
                scheduleDelayMs = 1,
                exportTimeoutMs = 1000,
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
                scheduleDelayMs = 1,
                exportTimeoutMs = 1000,
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
            exportTimeoutMs = 1,
            exportAction = {
                delay(2)
            }
        )
        assertEquals(emptyList(), exports)
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
        val processor = createProcessor(forceFlushTimeoutMs = 10, exportTimeoutMs = 50) {
            if (it == listOf(1)) {
                delay(100)
            }
            exports.add(it)
            OperationResultCode.Success
        }
        processor.processTelemetry(1)
        assertEquals(OperationResultCode.Failure, processor.forceFlush())

        // the worker recovers once the stuck export hits its timeout
        advanceTimeBy(100)
        processor.processTelemetry(2)
        assertEquals(OperationResultCode.Success, processor.forceFlush())
        assertEquals(listOf(listOf(2)), exports)

        processor.shutdown()
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
        exportTimeoutMs: Long = 1000,
        forceFlushTimeoutMs: Long = 1000,
        sdkErrorHandler: SdkErrorHandler = NoopSdkErrorHandler,
        flushAction: suspend () -> OperationResultCode = { OperationResultCode.Success },
        exportAction: suspend (List<Int>) -> OperationResultCode,
    ) = BatchTelemetryProcessor(
        config = BatchTelemetryConfig(
            maxQueueSize = 100,
            maxExportBatchSize = batchSize,
            scheduleDelayMs = scheduleDelayMs,
            exportTimeoutMs = exportTimeoutMs,
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
        exportTimeoutMs: Long = 1000,
        maxQueueSize: Int = 20,
        exportAction: suspend (telemetry: List<T>) -> Unit = {},
    ): List<List<T>> {
        val exports = mutableListOf<List<T>>()
        val dispatcher = StandardTestDispatcher(testScheduler)
        val processor = BatchTelemetryProcessor(
            config = BatchTelemetryConfig(
                maxQueueSize = maxQueueSize,
                maxExportBatchSize = batchSize,
                scheduleDelayMs = 1,
                exportTimeoutMs = exportTimeoutMs,
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
