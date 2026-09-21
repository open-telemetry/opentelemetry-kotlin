package io.opentelemetry.kotlin.export

import io.opentelemetry.kotlin.error.FakeSdkErrorHandler
import io.opentelemetry.kotlin.error.NoopSdkErrorHandler
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class BatchTelemetryConfigTest {

    @Test
    fun testDefaults() {
        val cfg = BatchTelemetryConfig(sdkErrorHandler = NoopSdkErrorHandler)
        assertEquals(BatchTelemetryDefaults.MAX_QUEUE_SIZE, cfg.maxQueueSize)
        assertEquals(BatchTelemetryDefaults.SPAN_SCHEDULE_DELAY_MS, cfg.scheduleDelayMs)
        assertEquals(BatchTelemetryDefaults.EXPORT_TIMEOUT_MS, cfg.exportTimeoutMs)
        assertEquals(BatchTelemetryDefaults.MAX_EXPORT_BATCH_SIZE, cfg.maxExportBatchSize)
        assertEquals(BatchTelemetryDefaults.FORCE_FLUSH_TIMEOUT_MS, cfg.forceFlushTimeoutMs)
    }

    @Test
    fun testInvalidConfigDoesNotThrow() {
        val handler = FakeSdkErrorHandler()
        val cfg = BatchTelemetryConfig(
            maxQueueSize = -1,
            scheduleDelayMs = -1,
            exportTimeoutMs = -1,
            maxExportBatchSize = -1,
            forceFlushTimeoutMs = -1,
            sdkErrorHandler = handler,
        )
        assertEquals(5, handler.apiMisuses.size)
        val default = BatchTelemetryConfig(sdkErrorHandler = NoopSdkErrorHandler)
        assertEquals(default.maxQueueSize, cfg.maxQueueSize)
        assertEquals(default.scheduleDelayMs, cfg.scheduleDelayMs)
        assertEquals(default.exportTimeoutMs, cfg.exportTimeoutMs)
        assertEquals(default.maxExportBatchSize, cfg.maxExportBatchSize)
        assertEquals(default.forceFlushTimeoutMs, cfg.forceFlushTimeoutMs)
    }

    @Test
    fun testZeroQueueBatchAndForceFlushFallBackToDefaults() {
        val handler = FakeSdkErrorHandler()
        val cfg = BatchTelemetryConfig(
            maxQueueSize = 0,
            maxExportBatchSize = 0,
            forceFlushTimeoutMs = 0,
            sdkErrorHandler = handler,
        )
        assertEquals(3, handler.apiMisuses.size)
        assertEquals(BatchTelemetryDefaults.MAX_QUEUE_SIZE, cfg.maxQueueSize)
        assertEquals(BatchTelemetryDefaults.MAX_EXPORT_BATCH_SIZE, cfg.maxExportBatchSize)
        assertEquals(BatchTelemetryDefaults.FORCE_FLUSH_TIMEOUT_MS, cfg.forceFlushTimeoutMs)
    }

    @Test
    fun testMaxExportBatchSizeCappedToQueueSize() {
        val handler = FakeSdkErrorHandler()
        val cfg = BatchTelemetryConfig(
            maxQueueSize = 10,
            maxExportBatchSize = 100,
            sdkErrorHandler = handler,
        )
        assertEquals(1, handler.apiMisuses.size)
        assertEquals(10, cfg.maxExportBatchSize)
        assertEquals(10, cfg.maxQueueSize)
    }

    @Test
    fun testZeroExportTimeoutMeansNoLimit() {
        val handler = FakeSdkErrorHandler()
        val cfg = BatchTelemetryConfig(
            exportTimeoutMs = 0,
            sdkErrorHandler = handler,
        )
        assertTrue(handler.apiMisuses.isEmpty())
        assertEquals(Long.MAX_VALUE, cfg.exportTimeoutMs)
    }
}
