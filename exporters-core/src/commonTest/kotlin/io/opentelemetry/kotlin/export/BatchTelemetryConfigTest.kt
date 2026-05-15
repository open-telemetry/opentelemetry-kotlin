package io.opentelemetry.kotlin.export

import io.opentelemetry.kotlin.error.FakeSdkErrorHandler
import kotlin.test.Test
import kotlin.test.assertEquals

internal class BatchTelemetryConfigTest {

    @Test
    fun testDefaults() {
        val cfg = BatchTelemetryConfig()
        assertEquals(BatchTelemetryDefaults.MAX_QUEUE_SIZE, cfg.maxQueueSize)
        assertEquals(BatchTelemetryDefaults.SCHEDULE_DELAY_MS, cfg.scheduleDelayMs)
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
        val default = BatchTelemetryConfig()
        assertEquals(default.maxQueueSize, cfg.maxQueueSize)
        assertEquals(default.scheduleDelayMs, cfg.scheduleDelayMs)
        assertEquals(default.exportTimeoutMs, cfg.exportTimeoutMs)
        assertEquals(default.maxExportBatchSize, cfg.maxExportBatchSize)
        assertEquals(default.forceFlushTimeoutMs, cfg.forceFlushTimeoutMs)
    }
}
