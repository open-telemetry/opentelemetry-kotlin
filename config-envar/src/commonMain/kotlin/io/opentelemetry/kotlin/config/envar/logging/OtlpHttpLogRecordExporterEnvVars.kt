package io.opentelemetry.kotlin.config.envar.logging

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.behavior.OtlpExporter
import io.opentelemetry.kotlin.behavior.OtlpHttpLogsExporterBehavior
import io.opentelemetry.kotlin.config.envar.OpenTelemetryEnvVars
import io.opentelemetry.kotlin.config.envar.reader.ReportingEnvVarReader

/**
 * Reads OTLP HTTP Log Exporter-specific environment variables and maps them to the corresponding
 * behavior configuration.
 */
@OptIn(ExperimentalApi::class)
class OtlpHttpLogRecordExporterEnvVars(private val reader: ReportingEnvVarReader) {
    fun toBehavior(): OtlpHttpLogsExporterBehavior {
        val signalEndpoint = reader.readString(OTLP_LOGS_ENDPOINT)
        val endpoint = reader.readString(OpenTelemetryEnvVars.OTLP_ENDPOINT)
        val timeout = reader.readNonNegativeLong(OTLP_LOGS_TIMEOUT)
            ?: reader.readNonNegativeLong(OpenTelemetryEnvVars.OTLP_TIMEOUT)
        val headers = OtlpExporter.buildHeaderMap(
            reader.readString(OTLP_LOGS_HEADERS)
                ?: reader.readString(OpenTelemetryEnvVars.OTLP_HEADERS)
        )
        return OtlpHttpLogsExporterBehavior(
            endpoint = endpoint,
            signalEndpoint = signalEndpoint,
            timeout = timeout,
            headers = headers,
        )
    }

    internal companion object {
        const val OTLP_LOGS_ENDPOINT = "OTEL_EXPORTER_OTLP_LOGS_ENDPOINT"
        const val OTLP_LOGS_TIMEOUT = "OTEL_EXPORTER_OTLP_LOGS_TIMEOUT"
        const val OTLP_LOGS_HEADERS = "OTEL_EXPORTER_OTLP_LOGS_HEADERS"
    }
}
