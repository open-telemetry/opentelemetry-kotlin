package io.opentelemetry.kotlin.config.envar.logging

import io.opentelemetry.kotlin.behavior.ConsoleExporterBehavior
import io.opentelemetry.kotlin.behavior.LogRecordExporterBehavior
import io.opentelemetry.kotlin.behavior.OtlpHttpLogsExporterBehavior
import io.opentelemetry.kotlin.config.envar.Exporter
import io.opentelemetry.kotlin.config.envar.reader.EnvVarReadWarning
import io.opentelemetry.kotlin.config.envar.reader.reportingEnvVarReader
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class LogRecordExporterEnvVarsTest {
    private val unknownExporter = "not_an_exporter"

    @Test
    fun `should map implemented exporters`() {
        var configs = mapOf(
            LogRecordExporterEnvVars.LOGS_EXPORTER to Exporter.CONSOLE.value,
        )
        assertEquals(
            LogRecordExporterBehavior(console = ConsoleExporterBehavior()),
            toBehavior(configs::get),
        )
        configs = mapOf(
            LogRecordExporterEnvVars.LOGS_EXPORTER to Exporter.OTLP.value,
            OtlpHttpLogRecordExporterEnvVars.OTLP_LOGS_ENDPOINT to "http://localhost:4318/v1/logs"
        )
        assertEquals(
            LogRecordExporterBehavior(http = OtlpHttpLogsExporterBehavior(signalEndpoint = "http://localhost:4318/v1/logs")),
            toBehavior(configs::get),
        )
    }

    @Test
    fun `should leave known non-implemented exporters unset`() {
        val exporters =
            listOf(Exporter.LOGGING, Exporter.NONE, Exporter.OTLP_STDOUT)
        exporters.forEach { exporter ->
            assertNull(
                toBehavior(mapOf(LogRecordExporterEnvVars.LOGS_EXPORTER to exporter.value)::get),
                "<$exporter> should not configure a processor"
            )
        }
    }

    @Test
    fun `should leave unknown exporter unset`() {
        val configs = mapOf(LogRecordExporterEnvVars.LOGS_EXPORTER to unknownExporter)
        assertNull(toBehavior(configs::get))
    }

    @Test
    fun `should warn on unknown exporter`() {
        val configs = mapOf(LogRecordExporterEnvVars.LOGS_EXPORTER to unknownExporter)
        val warnings = mutableListOf<EnvVarReadWarning>()
        LogRecordExporterEnvVars(reportingEnvVarReader(configs::get, warnings::add)).toBehavior()
        assertEquals(1, warnings.size)
        assertEquals(LogRecordExporterEnvVars.LOGS_EXPORTER, warnings.single().name)
    }

    @Test
    fun `should not warn when exporter is unset`() {
        val warnings = mutableListOf<EnvVarReadWarning>()
        LogRecordExporterEnvVars(reportingEnvVarReader({ null }, warnings::add)).toBehavior()
        assertEquals(emptyList(), warnings)
    }

    @Test
    fun `should not warn on known non-implemented exporters`() {
        val configs = mapOf(LogRecordExporterEnvVars.LOGS_EXPORTER to Exporter.LOGGING.value)
        val warnings = mutableListOf<EnvVarReadWarning>()
        LogRecordExporterEnvVars(reportingEnvVarReader(configs::get, warnings::add)).toBehavior()
        assertEquals(emptyList(), warnings)
    }

    private fun toBehavior(getEnvVar: (String) -> String?) =
        LogRecordExporterEnvVars(
            reportingEnvVarReader(getEnvVar),
        ).toBehavior()
}
