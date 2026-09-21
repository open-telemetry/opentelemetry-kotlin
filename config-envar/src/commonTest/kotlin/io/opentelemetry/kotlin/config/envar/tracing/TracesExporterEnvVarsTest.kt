package io.opentelemetry.kotlin.config.envar.tracing

import io.opentelemetry.kotlin.behavior.ConsoleExporterBehavior
import io.opentelemetry.kotlin.behavior.SpanProcessorBehavior
import io.opentelemetry.kotlin.config.envar.EnvVarReader
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class TracesExporterEnvVarsTest {

    @Test
    fun `should leave unset env vars unset`() {
        assertNull(toBehavior { null })
    }

    @Test
    fun `should map console`() {
        assertEquals(
            SpanProcessorBehavior(console = ConsoleExporterBehavior()),
            toBehavior(env("console")),
        )
    }

    @Test
    fun `should leave known non-console exporters unset`() {
        listOf("otlp", "logging", "none", "otlp/stdout", "").forEach { name ->
            assertNull(toBehavior(env(name)), "<$name> should not configure a processor")
        }
    }

    @Test
    fun `should leave unknown exporter unset`() {
        listOf("not_an_exporter", "zipkin").forEach { name ->
            assertNull(toBehavior(env(name)), "<$name> should not configure a processor")
        }
    }

    @Test
    fun `should warn on unknown exporter`() {
        listOf("not_an_exporter", "zipkin").forEach { name ->
            val warnings = mutableListOf<String>()
            TracesExporterEnvVars(EnvVarReader(env(name)), warnings::add).toBehavior()
            assertEquals(1, warnings.size, "<$name> should warn")
        }
    }

    @Test
    fun `should not warn when exporter is unset`() {
        val warnings = mutableListOf<String>()
        TracesExporterEnvVars(EnvVarReader { null }, warnings::add).toBehavior()
        assertEquals(emptyList(), warnings)
    }

    @Test
    fun `should not warn on known non-console exporters`() {
        val warnings = mutableListOf<String>()
        TracesExporterEnvVars(EnvVarReader(env("otlp")), warnings::add).toBehavior()
        assertEquals(emptyList(), warnings)
    }

    private fun env(exporter: String): (String) -> String? {
        val values = buildMap {
            put("OTEL_TRACES_EXPORTER", exporter)
        }
        return values::get
    }

    private fun toBehavior(getEnvVar: (String) -> String?) =
        TracesExporterEnvVars(EnvVarReader(getEnvVar)).toBehavior()
}
