package io.opentelemetry.kotlin.config.envar.tracing

import io.opentelemetry.kotlin.behavior.BatchSpanProcessorBehavior
import io.opentelemetry.kotlin.config.envar.reader.EnvVarReadWarning
import io.opentelemetry.kotlin.config.envar.reader.reportingEnvVarReader
import kotlin.test.Test
import kotlin.test.assertEquals

internal class BatchSpanProcessorEnvVarsTest {

    @Test
    fun readsBatchSettings() {
        val env = mapOf(
            BatchSpanProcessorEnvVars.SCHEDULE_DELAY to "100",
            BatchSpanProcessorEnvVars.EXPORT_TIMEOUT to "0",
            BatchSpanProcessorEnvVars.MAX_QUEUE_SIZE to "40",
            BatchSpanProcessorEnvVars.MAX_EXPORT_BATCH_SIZE to "20",
        )
        assertEquals(
            BatchSpanProcessorBehavior(100, 0, 40, 20),
            BatchSpanProcessorEnvVars(reportingEnvVarReader(env::get)).toBehavior(),
        )
    }

    @Test
    fun ignoresInvalidSizes() {
        val warnings = mutableListOf<EnvVarReadWarning>()
        val reader = reportingEnvVarReader({ "0" }, warnings::add)
        assertEquals(
            BatchSpanProcessorBehavior(scheduleDelay = 0, exportTimeout = 0),
            BatchSpanProcessorEnvVars(reader).toBehavior(),
        )
        assertEquals(2, warnings.size)
    }

    @Test
    fun leavesUnsetBatchSettingsUnset() {
        assertEquals(null, BatchSpanProcessorEnvVars(reportingEnvVarReader({ null })).toBehavior())
    }
}
