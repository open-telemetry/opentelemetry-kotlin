package io.opentelemetry.kotlin.config.envar.logging

import io.opentelemetry.kotlin.behavior.ConsoleExporterBehavior
import io.opentelemetry.kotlin.behavior.LogRecordProcessorBehavior
import io.opentelemetry.kotlin.behavior.OtlpHttpLogsExporterBehavior
import io.opentelemetry.kotlin.config.envar.Exporter
import io.opentelemetry.kotlin.config.envar.OpenTelemetryEnvVars
import io.opentelemetry.kotlin.config.envar.reader.EnvVarReadWarning
import io.opentelemetry.kotlin.config.envar.reader.reportingEnvVarReader
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class LogsExporterEnvVarsTest {
    private val unknownExporter = "not_an_exporter"

    @Test
    fun `should leave unset env vars unset`() {
        assertNull(toBehavior { null })
    }

    @Test
    fun `should map implemented exporters`() {
        var configs = mapOf(
            LogsExporterEnvVars.LOGS_EXPORTER to Exporter.CONSOLE.value,
        )
        assertEquals(
            LogRecordProcessorBehavior(console = ConsoleExporterBehavior()),
            toBehavior(configs::get),
        )

        configs = mapOf(
            LogsExporterEnvVars.LOGS_EXPORTER to Exporter.OTLP.value,
            OpenTelemetryEnvVars.OTLP_ENDPOINT to "http://localhost:4317",
            OpenTelemetryEnvVars.OTLP_TIMEOUT to "1",
            OpenTelemetryEnvVars.OTLP_HEADERS to "key1=value1,key2=value2",
        )
        assertEquals(
            LogRecordProcessorBehavior(
                http = OtlpHttpLogsExporterBehavior(
                    endpoint = "http://localhost:4317",
                    timeout = 1,
                    headers = mapOf("key1" to "value1", "key2" to "value2")
                )
            ),
            toBehavior(configs::get),
        )
    }

    @Test
    fun `should leave known non-implemented exporters unset`() {
        val exporters =
            listOf(Exporter.LOGGING, Exporter.NONE, Exporter.OTLP_STDOUT)
        exporters.forEach { exporter ->
            assertNull(
                toBehavior(mapOf(LogsExporterEnvVars.LOGS_EXPORTER to exporter.value)::get),
                "<$exporter> should not configure a processor"
            )
        }
    }

    @Test
    fun `should leave unknown exporter unset`() {
        val configs = mapOf(LogsExporterEnvVars.LOGS_EXPORTER to unknownExporter)
        assertNull(toBehavior(configs::get))
    }

    @Test
    fun `should warn on unknown exporter`() {
        val configs = mapOf(LogsExporterEnvVars.LOGS_EXPORTER to unknownExporter)
        val warnings = mutableListOf<EnvVarReadWarning>()
        LogsExporterEnvVars(reportingEnvVarReader(configs::get, warnings::add)).toBehavior()
        assertEquals(1, warnings.size)
        assertEquals(LogsExporterEnvVars.LOGS_EXPORTER, warnings.single().name)
    }

    @Test
    fun `should not warn when exporter is unset`() {
        val warnings = mutableListOf<EnvVarReadWarning>()
        LogsExporterEnvVars(reportingEnvVarReader({ null }, warnings::add)).toBehavior()
        assertEquals(emptyList(), warnings)
    }

    @Test
    fun `should not warn on known non-implemented exporters`() {
        val configs = mapOf(LogsExporterEnvVars.LOGS_EXPORTER to Exporter.LOGGING.value)
        val warnings = mutableListOf<EnvVarReadWarning>()
        LogsExporterEnvVars(reportingEnvVarReader(configs::get, warnings::add)).toBehavior()
        assertEquals(emptyList(), warnings)
    }

    @Test
    fun `Signal-specific configs override base configs`() {
        val configs = mutableMapOf(
            LogsExporterEnvVars.LOGS_EXPORTER to Exporter.OTLP.value,
            OpenTelemetryEnvVars.OTLP_ENDPOINT to "http://localhost:4317",
            OpenTelemetryEnvVars.OTLP_TIMEOUT to "1",
            OpenTelemetryEnvVars.OTLP_HEADERS to "key1=value1,key2=value2"
        )
        assertEquals(
            LogRecordProcessorBehavior(
                http = OtlpHttpLogsExporterBehavior(
                    endpoint = "http://localhost:4317",
                    timeout = 1,
                    headers = mapOf("key1" to "value1", "key2" to "value2")
                )
            ),
            toBehavior(configs::get),
        )
        configs.putAll(
            mapOf(
                LogsExporterEnvVars.OTLP_LOGS_ENDPOINT to "http://localhost:4317/logs",
                LogsExporterEnvVars.OTLP_LOGS_TIMEOUT to "2",
                LogsExporterEnvVars.OTLP_LOGS_HEADERS to "key3=value3,key4=value4"
            )
        )
        assertEquals(
            LogRecordProcessorBehavior(
                http = OtlpHttpLogsExporterBehavior(
                    endpoint = "http://localhost:4317/logs",
                    timeout = 2,
                    headers = mapOf("key3" to "value3", "key4" to "value4")
                )
            ),
            toBehavior(configs::get),
        )
    }

    @Test
    fun `should skip malformed header entries`() {
        val configs = mapOf(
            LogsExporterEnvVars.LOGS_EXPORTER to Exporter.OTLP.value,
            OpenTelemetryEnvVars.OTLP_HEADERS to "key1=value1,malformed,key2=value2",
        )
        assertEquals(
            LogRecordProcessorBehavior(
                http = OtlpHttpLogsExporterBehavior(
                    headers = mapOf("key1" to "value1", "key2" to "value2")
                )
            ),
            toBehavior(configs::get),
        )
    }

    private fun toBehavior(getEnvVar: (String) -> String?) =
        LogsExporterEnvVars(
            reportingEnvVarReader(getEnvVar),
        ).toBehavior()
}
