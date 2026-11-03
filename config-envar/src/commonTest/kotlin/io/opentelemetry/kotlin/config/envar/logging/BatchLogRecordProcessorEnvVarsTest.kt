package io.opentelemetry.kotlin.config.envar.logging

import io.opentelemetry.kotlin.behavior.BatchLogRecordProcessorBehavior
import io.opentelemetry.kotlin.behavior.ConsoleExporterBehavior
import io.opentelemetry.kotlin.behavior.LogRecordExporterBehavior
import io.opentelemetry.kotlin.config.envar.Exporter
import io.opentelemetry.kotlin.config.envar.reader.EnvVarReadWarning
import io.opentelemetry.kotlin.config.envar.reader.reportingEnvVarReader
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class BatchLogRecordProcessorEnvVarsTest {
    @Test
    fun readBatchSettings() {
        val env = mapOf(
            "OTEL_BLRP_SCHEDULE_DELAY" to "100",
            "OTEL_BLRP_EXPORT_TIMEOUT" to "0",
            "OTEL_BLRP_MAX_QUEUE_SIZE" to "40",
            "OTEL_BLRP_MAX_EXPORT_BATCH_SIZE" to "20",
            "OTEL_LOGS_EXPORTER" to Exporter.CONSOLE.toString(),
        )
        assertEquals(
            BatchLogRecordProcessorBehavior(
                scheduleDelay = 100,
                exportTimeout = 0,
                maxQueueSize = 40,
                maxExportBatchSize = 20,
                exporter = LogRecordExporterBehavior(
                    console = ConsoleExporterBehavior(),
                ),
            ),
            BatchLogRecordProcessorEnvVars(reportingEnvVarReader(env::get)).toBehavior(),
        )
    }

    @Test
    fun ignoresInvalidSizes() {
        val warnings = mutableListOf<EnvVarReadWarning>()
        val reader = reportingEnvVarReader({ "0" }, warnings::add)
        assertEquals(
            BatchLogRecordProcessorBehavior(
                scheduleDelay = 0,
                exportTimeout = 0,
                maxQueueSize = null,
                maxExportBatchSize = null,
                exporter = null,
            ),
            BatchLogRecordProcessorEnvVars(reader).toBehavior(),
        )
        assertEquals(3, warnings.size)
    }

    @Test
    fun leavesUnsetBatchSettingsUnset() {
        assertNull(BatchLogRecordProcessorEnvVars(reportingEnvVarReader({ null })).toBehavior())
    }

    @Test
    fun ignoresBatchSpanProcessorSettings() {
        val env = mapOf(
            "OTEL_BSP_SCHEDULE_DELAY" to "100",
            "OTEL_BSP_EXPORT_TIMEOUT" to "200",
            "OTEL_BSP_MAX_QUEUE_SIZE" to "40",
            "OTEL_BSP_MAX_EXPORT_BATCH_SIZE" to "20",
        )
        assertNull(BatchLogRecordProcessorEnvVars(reportingEnvVarReader(env::get)).toBehavior())
    }
}
