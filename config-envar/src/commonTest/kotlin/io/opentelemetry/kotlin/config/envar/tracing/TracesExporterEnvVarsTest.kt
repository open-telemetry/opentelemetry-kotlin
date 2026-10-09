package io.opentelemetry.kotlin.config.envar.tracing

import io.opentelemetry.kotlin.behavior.ConsoleExporterBehavior
import io.opentelemetry.kotlin.behavior.OtlpHttpSpanExporterBehavior
import io.opentelemetry.kotlin.behavior.SpanProcessorBehavior
import io.opentelemetry.kotlin.config.envar.Exporter
import io.opentelemetry.kotlin.config.envar.reader.EnvVarReadWarning
import io.opentelemetry.kotlin.config.envar.reader.reportingEnvVarReader
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class TracesExporterEnvVarsTest {
    private val unknownExporter = "not_an_exporter"

    @Test
    fun `should leave unset env vars unset`() {
        assertNull(toBehavior { null })
    }

    @Test
    fun `should map implemented exporters`() {
        var configs = mapOf(
            TracesExporterEnvVars.TRACES_EXPORTER to Exporter.CONSOLE.value
        )
        assertEquals(
            SpanProcessorBehavior(console = ConsoleExporterBehavior()),
            toBehavior(configs::get),
        )

        configs = mapOf(
            TracesExporterEnvVars.TRACES_EXPORTER to Exporter.OTLP.value,
        )
        assertEquals(
            SpanProcessorBehavior(http = OtlpHttpSpanExporterBehavior()),
            toBehavior(configs::get)
        )
    }

    @Test
    fun `should leave known non-implemented exporters unset`() {
        val exporters = listOf(Exporter.LOGGING, Exporter.NONE, Exporter.OTLP_STDOUT)
        exporters.forEach { exporter ->
            assertNull(
                toBehavior(mapOf(TracesExporterEnvVars.TRACES_EXPORTER to exporter.value)::get),
                "<$exporter> should not configure a processor"
            )
        }
    }

    @Test
    fun `should leave unknown exporter unset`() {
        val configs = mapOf(TracesExporterEnvVars.TRACES_EXPORTER to unknownExporter)
        assertNull(toBehavior(configs::get))
    }

    @Test
    fun `should warn on unknown exporter`() {
        val configs = mapOf(TracesExporterEnvVars.TRACES_EXPORTER to unknownExporter)
        val warnings = mutableListOf<EnvVarReadWarning>()
        TracesExporterEnvVars(reportingEnvVarReader(configs::get, warnings::add)).toBehavior()
        assertEquals(1, warnings.size)
        assertEquals(TracesExporterEnvVars.TRACES_EXPORTER, warnings.single().name)
    }

    @Test
    fun `should not warn when exporter is unset`() {
        val warnings = mutableListOf<EnvVarReadWarning>()
        TracesExporterEnvVars(reportingEnvVarReader({ null }, warnings::add)).toBehavior()
        assertEquals(emptyList(), warnings)
    }

    @Test
    fun `should not warn on known non-implemented exporters`() {
        val configs = mapOf(TracesExporterEnvVars.TRACES_EXPORTER to Exporter.OTLP.value)
        val warnings = mutableListOf<EnvVarReadWarning>()
        TracesExporterEnvVars(reportingEnvVarReader(configs::get, warnings::add)).toBehavior()
        assertEquals(emptyList(), warnings)
    }

    private fun toBehavior(getEnvVar: (String) -> String?) =
        TracesExporterEnvVars(reportingEnvVarReader(getEnvVar)).toBehavior()
}
