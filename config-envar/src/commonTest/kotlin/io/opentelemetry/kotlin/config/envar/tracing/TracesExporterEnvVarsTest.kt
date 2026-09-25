package io.opentelemetry.kotlin.config.envar.tracing

import io.opentelemetry.kotlin.behavior.ConsoleExporterBehavior
import io.opentelemetry.kotlin.behavior.OtlpHttpExporterBehavior
import io.opentelemetry.kotlin.behavior.SpanProcessorBehavior
import io.opentelemetry.kotlin.config.envar.OpenTelemetryEnvVars
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
        assertEquals(
            SpanProcessorBehavior(console = ConsoleExporterBehavior()),
            toBehavior(env(TracesExporterEnvVars.CONSOLE)),
        )
        assertEquals(
            SpanProcessorBehavior(http = OtlpHttpExporterBehavior()),
            toBehavior(env(TracesExporterEnvVars.OTLP)),
        )
    }

    @Test
    fun `should leave known non-implemented exporters unset`() {
        val exporters =
            listOf(TracesExporterEnvVars.LOGGING, TracesExporterEnvVars.NONE, TracesExporterEnvVars.OTLP_STDOUT, "")
        exporters.forEach { name ->
            assertNull(
                toBehavior(env(name)),
                "<$name> should not configure a processor"
            )
        }
    }

    @Test
    fun `should leave unknown exporter unset`() {
        assertNull(toBehavior(env(unknownExporter)))
    }

    @Test
    fun `should warn on unknown exporter`() {
        val warnings = mutableListOf<EnvVarReadWarning>()
        TracesExporterEnvVars(reportingEnvVarReader(env(unknownExporter), warnings::add)).toBehavior()
        assertEquals(1, warnings.size)
        assertEquals(TracesExporterEnvVars.EXPORTER, warnings.single().name)
    }

    @Test
    fun `should not warn when exporter is unset`() {
        val warnings = mutableListOf<EnvVarReadWarning>()
        TracesExporterEnvVars(reportingEnvVarReader({ null }, warnings::add)).toBehavior()
        assertEquals(emptyList(), warnings)
    }

    @Test
    fun `should not warn on known non-implemented exporters`() {
        val warnings = mutableListOf<EnvVarReadWarning>()
        TracesExporterEnvVars(reportingEnvVarReader(env(TracesExporterEnvVars.OTLP), warnings::add)).toBehavior()
        assertEquals(emptyList(), warnings)
    }

    @Test
    fun `Signal-specific configs override base configs`() {
        val configs = mutableMapOf(
            TracesExporterEnvVars.EXPORTER to TracesExporterEnvVars.OTLP,
            OpenTelemetryEnvVars.OTLP_ENDPOINT to "http://localhost:4317",
            OpenTelemetryEnvVars.OTLP_TIMEOUT to "1",
        )
        assertEquals(
            SpanProcessorBehavior(
                http = OtlpHttpExporterBehavior(
                    endpoint = "http://localhost:4317",
                    timeout = 1
                )
            ),
            toBehavior(configs::get),
        )
        configs.putAll(
            mapOf(
                TracesExporterEnvVars.OTLP_TRACES_ENDPOINT to "http://localhost:4317/traces",
                TracesExporterEnvVars.OTLP_TRACES_TIMEOUT to "2"
            )
        )
        assertEquals(
            SpanProcessorBehavior(
                http = OtlpHttpExporterBehavior(
                    endpoint = "http://localhost:4317/traces",
                    timeout = 2
                )
            ),
            toBehavior(configs::get),
        )
    }

    private fun env(exporter: String): (String) -> String? {
        val values = buildMap {
            put(TracesExporterEnvVars.EXPORTER, exporter)
        }
        return values::get
    }

    private fun toBehavior(getEnvVar: (String) -> String?) =
        TracesExporterEnvVars(reportingEnvVarReader(getEnvVar)).toBehavior()
}
