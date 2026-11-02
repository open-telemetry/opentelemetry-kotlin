package io.opentelemetry.kotlin.config.envar.logging

import io.opentelemetry.kotlin.behavior.BatchLogRecordProcessorBehavior
import io.opentelemetry.kotlin.behavior.LogRecordProcessorBehavior
import io.opentelemetry.kotlin.config.envar.reader.reportingEnvVarReader
import kotlin.test.Test
import kotlin.test.assertEquals

internal class LogRecordProcessorEnvVarsTest {
    @Test
    fun `should map batch processor`() {
        val configs = mapOf(
            "OTEL_BLRP_SCHEDULE_DELAY" to "1",
            "OTEL_BLRP_EXPORT_TIMEOUT" to "2",
            "OTEL_BLRP_MAX_QUEUE_SIZE" to "3",
            "OTEL_BLRP_MAX_EXPORT_BATCH_SIZE" to "4",
        )
        assertEquals(
            LogRecordProcessorBehavior(
                batch = BatchLogRecordProcessorBehavior(
                    scheduleDelay = 1,
                    exportTimeout = 2,
                    maxQueueSize = 3,
                    maxExportBatchSize = 4,
                )
            ),
            toBehavior(configs::get),
        )
    }

    private fun toBehavior(getEnvVar: (String) -> String?) =
        LogRecordProcessorEnvVars(
            reportingEnvVarReader(getEnvVar)
        ).toBehavior()
}
