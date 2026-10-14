package io.opentelemetry.kotlin.config.envar.logging

import io.opentelemetry.kotlin.behavior.OtlpHttpLogsExporterBehavior
import io.opentelemetry.kotlin.config.envar.OpenTelemetryEnvVars
import io.opentelemetry.kotlin.config.envar.reader.reportingEnvVarReader
import kotlin.test.Test
import kotlin.test.assertEquals

internal class OtlpHttpLogsExporterEnvVarsTest {
    @Test
    fun `maps OTLP HTTP span exporter settings`() {
        val configs = mapOf(
            OpenTelemetryEnvVars.OTLP_ENDPOINT to "http://localhost:4317",
            OpenTelemetryEnvVars.OTLP_TIMEOUT to "10000",
            OpenTelemetryEnvVars.OTLP_HEADERS to "key1=value1,key2=value2",
        )
        assertEquals(
            OtlpHttpLogsExporterBehavior(
                endpoint = "http://localhost:4317",
                timeout = 10_000,
                headers = mapOf("key1" to "value1", "key2" to "value2")
            ),
            toBehavior(configs::get),
        )
    }

    @Test
    fun `Signal-specific configs override base configs`() {
        val configs = mutableMapOf(
            OpenTelemetryEnvVars.OTLP_ENDPOINT to "http://localhost:4317",
            OpenTelemetryEnvVars.OTLP_TIMEOUT to "100",
            OpenTelemetryEnvVars.OTLP_HEADERS to "key1=value1,key2=value2",
        )
        assertEquals(
            OtlpHttpLogsExporterBehavior(
                endpoint = "http://localhost:4317",
                timeout = 100,
                headers = mapOf("key1" to "value1", "key2" to "value2"),
            ),
            toBehavior(configs::get),
        )
        configs.putAll(
            mapOf(
                LogsExporterEnvVars.OTLP_LOGS_ENDPOINT to "http://localhost:4317/traces",
                LogsExporterEnvVars.OTLP_LOGS_TIMEOUT to "2",
                LogsExporterEnvVars.OTLP_LOGS_HEADERS to "key3=value3,key4=value4",
            )
        )
        assertEquals(
            OtlpHttpLogsExporterBehavior(
                endpoint = "http://localhost:4317/traces",
                timeout = 2,
                headers = mapOf("key3" to "value3", "key4" to "value4")
            ),
            toBehavior(configs::get)
        )
    }

    @Test
    fun `should skip malformed header entries`() {
        val configs = mapOf(
            OpenTelemetryEnvVars.OTLP_HEADERS to "key1=value1,malformed,key2=value2",
        )
        assertEquals(
            OtlpHttpLogsExporterBehavior(headers = mapOf("key1" to "value1", "key2" to "value2")),
            toBehavior(configs::get),
        )
    }

    private fun toBehavior(getEnvVar: (String) -> String?) =
        OtlpHttpLogsExporterEnvVars(reportingEnvVarReader(getEnvVar)).toBehavior()
}
