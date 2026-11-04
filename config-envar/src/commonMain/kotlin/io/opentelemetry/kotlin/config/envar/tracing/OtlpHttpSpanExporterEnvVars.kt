package io.opentelemetry.kotlin.config.envar.tracing

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.behavior.OtlpExporter
import io.opentelemetry.kotlin.behavior.OtlpHttpSpanExporterBehavior
import io.opentelemetry.kotlin.config.envar.OpenTelemetryEnvVars
import io.opentelemetry.kotlin.config.envar.reader.ReportingEnvVarReader

/**
 * Reads OTLP HTTP Span Exporter-specific environment variables and maps them to the corresponding
 * behavior configuration.
 */
@ExperimentalApi
class OtlpHttpSpanExporterEnvVars(private val reader: ReportingEnvVarReader) {
    fun toBehavior(): OtlpHttpSpanExporterBehavior {
        val signalEndpoint = reader.readString(TracesExporterEnvVars.OTLP_TRACES_ENDPOINT)
        val endpoint = reader.readString(OpenTelemetryEnvVars.OTLP_ENDPOINT)
        val timeout = reader.readNonNegativeLong(TracesExporterEnvVars.OTLP_TRACES_TIMEOUT)
            ?: reader.readNonNegativeLong(OpenTelemetryEnvVars.OTLP_TIMEOUT)
        val headers = OtlpExporter.buildHeaderMap(
            reader.readString(TracesExporterEnvVars.OTLP_TRACES_HEADERS)
                ?: reader.readString(OpenTelemetryEnvVars.OTLP_HEADERS)
        )
        return OtlpHttpSpanExporterBehavior(
            endpoint = endpoint,
            signalEndpoint = signalEndpoint,
            timeout = timeout,
            headers = headers,
        )
    }
}
