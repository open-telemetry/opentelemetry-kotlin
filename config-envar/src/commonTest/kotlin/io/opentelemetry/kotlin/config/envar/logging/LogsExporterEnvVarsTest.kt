package io.opentelemetry.kotlin.config.envar.logging

import io.opentelemetry.kotlin.behavior.ConsoleExporterBehavior
import io.opentelemetry.kotlin.behavior.LogRecordProcessorBehavior
import io.opentelemetry.kotlin.config.envar.EnvVarReader
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class LogsExporterEnvVarsTest {

    @Test
    fun `should leave unset env vars unset`() {
        assertNull(toBehavior { null })
    }

    @Test
    fun `should map console`() {
        assertEquals(
            LogRecordProcessorBehavior(console = ConsoleExporterBehavior()),
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
        assertNull(toBehavior(env("not_an_exporter")))
    }

    @Test
    fun `should warn on unknown exporter`() {
        val warnings = mutableListOf<String>()
        LogsExporterEnvVars(EnvVarReader(env("not_an_exporter")), warnings::add).toBehavior()
        assertEquals(1, warnings.size)
    }

    @Test
    fun `should not warn when exporter is unset`() {
        val warnings = mutableListOf<String>()
        LogsExporterEnvVars(EnvVarReader { null }, warnings::add).toBehavior()
        assertEquals(emptyList(), warnings)
    }

    @Test
    fun `should not warn on known non-console exporters`() {
        val warnings = mutableListOf<String>()
        LogsExporterEnvVars(EnvVarReader(env("otlp")), warnings::add).toBehavior()
        assertEquals(emptyList(), warnings)
    }

    private fun env(exporter: String): (String) -> String? {
        val values = buildMap {
            put("OTEL_LOGS_EXPORTER", exporter)
        }
        return values::get
    }

    private fun toBehavior(getEnvVar: (String) -> String?) =
        LogsExporterEnvVars(EnvVarReader(getEnvVar)).toBehavior()
}
