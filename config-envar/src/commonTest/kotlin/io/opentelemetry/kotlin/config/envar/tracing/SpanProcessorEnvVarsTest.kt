package io.opentelemetry.kotlin.config.envar.tracing

import io.opentelemetry.kotlin.behavior.BatchSpanProcessorBehavior
import io.opentelemetry.kotlin.behavior.ConsoleExporterBehavior
import io.opentelemetry.kotlin.behavior.SpanExporterBehavior
import io.opentelemetry.kotlin.behavior.SpanProcessorBehavior
import io.opentelemetry.kotlin.config.envar.Exporter
import io.opentelemetry.kotlin.config.envar.reader.reportingEnvVarReader
import kotlin.test.Test
import kotlin.test.assertEquals

internal class SpanProcessorEnvVarsTest {
    @Test
    fun `should leave unset env vars unset`() {
        assertEquals(toBehavior { null }, SpanProcessorBehavior())
    }

    @Test
    fun `maps span processor fields`() {
        val env = mapOf(
            BatchSpanProcessorEnvVars.SCHEDULE_DELAY to "100",
            BatchSpanProcessorEnvVars.EXPORT_TIMEOUT to "0",
            BatchSpanProcessorEnvVars.MAX_QUEUE_SIZE to "40",
            BatchSpanProcessorEnvVars.MAX_EXPORT_BATCH_SIZE to "20",
            SpanExporterEnvVars.TRACES_EXPORTER to Exporter.CONSOLE.toString(),
        )
        assertEquals(
            SpanProcessorBehavior(
                batch = BatchSpanProcessorBehavior(
                    scheduleDelay = 100,
                    exportTimeout = 0,
                    maxQueueSize = 40,
                    maxExportBatchSize = 20,
                    SpanExporterBehavior(
                        console = ConsoleExporterBehavior()
                    )
                )
            ),
            toBehavior(env::get)
        )
    }

    private fun toBehavior(getEnvVar: (String) -> String?) =
        SpanProcessorEnvVars(reportingEnvVarReader(getEnvVar)).toBehavior()
}
