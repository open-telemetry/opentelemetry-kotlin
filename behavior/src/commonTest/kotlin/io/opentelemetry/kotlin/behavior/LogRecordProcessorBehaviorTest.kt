package io.opentelemetry.kotlin.behavior

import io.opentelemetry.kotlin.behavior.logging.BatchLogRecordProcessorBehavior
import io.opentelemetry.kotlin.behavior.logging.LogRecordExporterBehavior
import io.opentelemetry.kotlin.behavior.logging.LogRecordProcessorBehavior
import io.opentelemetry.kotlin.behavior.logging.OtlpHttpLogsExporterBehavior
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class LogRecordProcessorBehaviorTest {

    @Test
    fun everyFieldsStartsUnset() {
        val processor = LogRecordProcessorBehavior()
        assertNull(processor.batch)
        assertNull(processor.simple)
    }

    @Test
    fun adoptsEverythingWhenLowerIsUnset() {
        val higher = LogRecordProcessorBehavior(
            batch = BatchLogRecordProcessorBehavior(
                scheduleDelay = 1_000,
                exportTimeout = 1_000,
                maxQueueSize = 1_000,
                maxExportBatchSize = 1_000,
                exporter = LogRecordExporterBehavior(
                    console = ConsoleExporterBehavior()
                )
            )
        )
        assertEquals(higher, LogRecordProcessorBehavior().mergeWith(higher))
    }

    @Test
    fun prefersHigherLayerForEveryField() {
        val lower = LogRecordProcessorBehavior(
            batch = BatchLogRecordProcessorBehavior(
                scheduleDelay = 100,
                exportTimeout = 100,
                maxQueueSize = 100,
                maxExportBatchSize = 100,
                exporter = LogRecordExporterBehavior(
                    http = OtlpHttpLogsExporterBehavior(
                        endpoint = "http://localhost:8080/lower"
                    )
                )
            )
        )
        val higher = LogRecordProcessorBehavior(
            batch = BatchLogRecordProcessorBehavior(
                scheduleDelay = 1_000,
                exportTimeout = 1_000,
                maxQueueSize = 1_000,
                maxExportBatchSize = 1_000,
                exporter = LogRecordExporterBehavior(
                    http = OtlpHttpLogsExporterBehavior(
                        endpoint = "http://localhost:8080/higher"
                    )
                )
            )
        )
        assertEquals(higher, lower.mergeWith(higher))
    }
}
