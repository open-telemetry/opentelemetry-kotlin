package io.opentelemetry.kotlin.config.envar.tracing

import io.opentelemetry.kotlin.behavior.OtlpHttpSpanExporterBehavior
import io.opentelemetry.kotlin.config.envar.OpenTelemetryEnvVars
import io.opentelemetry.kotlin.config.envar.reader.reportingEnvVarReader
import kotlin.test.Test
import kotlin.test.assertEquals

internal class OtlpHttpSpanExporterEnvVarsTest {
    @Test
    fun `maps OTLP HTTP span exporter settings`() {
        val configs = mapOf(
            OpenTelemetryEnvVars.OTLP_ENDPOINT to "http://localhost:4317",
            OpenTelemetryEnvVars.OTLP_TIMEOUT to "10000",
            OpenTelemetryEnvVars.OTLP_HEADERS to "key1=value1,key2=value2",
        )
        assertEquals(
            OtlpHttpSpanExporterBehavior(
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
            OtlpHttpSpanExporterBehavior(
                endpoint = "http://localhost:4317",
                timeout = 100,
                headers = mapOf("key1" to "value1", "key2" to "value2"),
            ),
            toBehavior(configs::get),
        )
        configs.putAll(
            mapOf(
                TracesExporterEnvVars.OTLP_TRACES_ENDPOINT to "http://localhost:4317/traces",
                TracesExporterEnvVars.OTLP_TRACES_TIMEOUT to "2",
                TracesExporterEnvVars.OTLP_TRACES_HEADERS to "key3=value3,key4=value4",
            )
        )
        assertEquals(
            OtlpHttpSpanExporterBehavior(
                endpoint = "http://localhost:4317",
                signalEndpoint = "http://localhost:4317/traces",
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
            OtlpHttpSpanExporterBehavior(headers = mapOf("key1" to "value1", "key2" to "value2")),
            toBehavior(configs::get),
        )
    }

    @Test
    fun `spec example 1 - general base endpoint only`() {
        val configs = mapOf(
            OpenTelemetryEnvVars.OTLP_ENDPOINT to "http://collector:4318",
        )
        val behavior = toBehavior(configs::get)
        assertEquals("http://collector:4318", behavior.endpoint)
        assertEquals(null, behavior.signalEndpoint)
    }

    @Test
    fun `spec example 2 - custom base path for all signals`() {
        val configs = mapOf(
            OpenTelemetryEnvVars.OTLP_ENDPOINT to "http://collector:4318/mycollector/",
        )
        val behavior = toBehavior(configs::get)
        assertEquals("http://collector:4318/mycollector/", behavior.endpoint)
        assertEquals(null, behavior.signalEndpoint)
    }

    @Test
    fun `spec example 3 - mixing base endpoint with per-signal overrides`() {
        val configs = mapOf(
            OpenTelemetryEnvVars.OTLP_ENDPOINT to "http://collector:4318/mycollector/",
            TracesExporterEnvVars.OTLP_TRACES_ENDPOINT to "https://collector.example.com/v1/traces/",
        )
        val behavior = toBehavior(configs::get)
        assertEquals("http://collector:4318/mycollector/", behavior.endpoint)
        assertEquals("https://collector.example.com/v1/traces/", behavior.signalEndpoint)
    }

    private fun toBehavior(getEnvVar: (String) -> String?) =
        OtlpHttpSpanExporterEnvVars(reportingEnvVarReader(getEnvVar)).toBehavior()
}
