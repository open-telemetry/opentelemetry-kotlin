package io.opentelemetry.kotlin.config.envar.tracing

import io.opentelemetry.kotlin.behavior.SpanExporterBehavior
import io.opentelemetry.kotlin.config.envar.reader.EnvVarReadWarning
import io.opentelemetry.kotlin.config.envar.reader.reportingEnvVarReader
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class TracesExporterEnvVarsTest {

    @Test
    fun `should leave unset env vars unset`() {
        assertNull(toBehavior { null })
    }

    @Test
    fun `should map console to ConsoleExporter`() {
        assertEquals(SpanExporterBehavior.Console, toBehavior(env("console")))
    }

    @Test
    fun `should leave known non-console exporters unset`() {
        listOf("otlp", "logging", "none", "otlp/stdout", "").forEach { name ->
            assertNull(toBehavior(env(name)), "<$name> should not configure an exporter")
        }
    }

    @Test
    fun `should leave unknown exporter unset`() {
        listOf("not_an_exporter", "zipkin").forEach { name ->
            assertNull(toBehavior(env(name)), "<$name> should not configure an exporter")
        }
    }

    @Test
    fun `should warn on unknown exporter`() {
        listOf("not_an_exporter", "zipkin").forEach { name ->
            val warnings = mutableListOf<EnvVarReadWarning>()
            TracesExporterEnvVars(reportingEnvVarReader(env(name), warnings::add)).toBehavior()
            assertEquals(1, warnings.size, "<$name> should warn")
            assertEquals("OTEL_TRACES_EXPORTER", warnings.single().name)
        }
    }

    @Test
    fun `should not warn when exporter is unset`() {
        val warnings = mutableListOf<EnvVarReadWarning>()
        TracesExporterEnvVars(reportingEnvVarReader({ null }, warnings::add)).toBehavior()
        assertEquals(emptyList(), warnings)
    }

    @Test
    fun `should not warn on known non-console exporters`() {
        val warnings = mutableListOf<EnvVarReadWarning>()
        TracesExporterEnvVars(reportingEnvVarReader(env("otlp"), warnings::add)).toBehavior()
        assertEquals(emptyList(), warnings)
    }

    private fun env(exporter: String): (String) -> String? {
        val values = buildMap {
            put("OTEL_TRACES_EXPORTER", exporter)
        }
        return values::get
    }

    private fun toBehavior(getEnvVar: (String) -> String?) =
        TracesExporterEnvVars(reportingEnvVarReader(getEnvVar)).toBehavior()
}
