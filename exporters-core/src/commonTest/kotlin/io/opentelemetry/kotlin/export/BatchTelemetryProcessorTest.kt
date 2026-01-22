package io.opentelemetry.kotlin.export

import io.opentelemetry.kotlin.ExperimentalApi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

@OptIn(ExperimentalApi::class, ExperimentalCoroutinesApi::class)
internal class BatchTelemetryProcessorTest {

    @Test
    fun testInvalidMaxQueueSize() {
        assertFailsWith<IllegalArgumentException> {
            BatchTelemetryProcessor<Unit>(
                maxQueueSize = -1,
                scheduleDelayMs = 1,
                exportTimeoutMs = 1,
                maxExportBatchSize = 1,
                exportAction = { OperationResultCode.Success }
            )
        }
    }

    @Test
    fun testInvalidScheduleDelayMs() {
        assertFailsWith<IllegalArgumentException> {
            BatchTelemetryProcessor<Unit>(
                maxQueueSize = 1,
                scheduleDelayMs = -1,
                exportTimeoutMs = 1,
                maxExportBatchSize = 1,
                exportAction = { OperationResultCode.Success }
            )
        }
    }

    @Test
    fun testInvalidExportTimeoutMs() {
        assertFailsWith<IllegalArgumentException> {
            BatchTelemetryProcessor<Unit>(
                maxQueueSize = 1,
                scheduleDelayMs = 1,
                exportTimeoutMs = -1,
                maxExportBatchSize = 1,
                exportAction = { OperationResultCode.Success }
            )
        }
    }

    @Test
    fun testInvalidMaxExportBatchSize() {
        assertFailsWith<IllegalArgumentException> {
            BatchTelemetryProcessor<Unit>(
                maxQueueSize = 1,
                scheduleDelayMs = 1,
                exportTimeoutMs = 1,
                maxExportBatchSize = -1,
                exportAction = { OperationResultCode.Success }
            )
        }
    }

    @Test
    fun testInvalidMaxExportBatchSize2() {
        assertFailsWith<IllegalArgumentException> {
            BatchTelemetryProcessor<Unit>(
                maxQueueSize = 100,
                scheduleDelayMs = 1,
                exportTimeoutMs = 1,
                maxExportBatchSize = 200,
                exportAction = { OperationResultCode.Success }
            )
        }
    }

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
            maxQueueSize = 100,
            maxExportBatchSize = 1,
            scheduleDelayMs = 1,
            exportTimeoutMs = 1000,
            dispatcher = dispatcher,
            exportAction = {
                exports.add(it)
                OperationResultCode.Success
            }
        )

        processor.processTelemetry(1)

        // TODO: dry
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
    fun testQueueSaturation() = runTest {
        val sendAttempts = 1000
        val exports = assertTelemetryBatched(
            telemetry = (0..sendAttempts).toList()
        )
        val sent = exports.flatten()
        assertTrue(sent.size < sendAttempts)
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

    private suspend fun <T> TestScope.assertTelemetryBatched(
        telemetry: List<T>,
        batchSize: Int = 3,
        exportTimeoutMs: Long = 1000,
        exportAction: suspend (telemetry: List<T>) -> Unit = {},
    ): List<List<T>> {
        val exports = mutableListOf<List<T>>()
        val dispatcher = StandardTestDispatcher(testScheduler)
        val processor = BatchTelemetryProcessor(
            maxQueueSize = 20,
            maxExportBatchSize = batchSize,
            scheduleDelayMs = 1,
            exportTimeoutMs = exportTimeoutMs,
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
