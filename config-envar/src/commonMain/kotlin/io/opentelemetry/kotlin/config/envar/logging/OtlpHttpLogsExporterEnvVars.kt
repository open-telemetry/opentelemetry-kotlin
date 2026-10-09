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
@ExperimentalApi
class OtlpHttpLogsExporterEnvVars(private val reader: ReportingEnvVarReader) {
    fun toBehavior(): OtlpHttpLogsExporterBehavior {
        val endpoint = reader.readString(LogsExporterEnvVars.OTLP_LOGS_ENDPOINT)
            ?: reader.readString(OpenTelemetryEnvVars.OTLP_ENDPOINT)
        val timeout = reader.readNonNegativeLong(LogsExporterEnvVars.OTLP_LOGS_TIMEOUT)
            ?: reader.readNonNegativeLong(OpenTelemetryEnvVars.OTLP_TIMEOUT)
        val headers = OtlpExporter.buildHeaderMap(
            reader.readString(LogsExporterEnvVars.OTLP_LOGS_HEADERS)
                ?: reader.readString(OpenTelemetryEnvVars.OTLP_HEADERS)
        )
        return OtlpHttpLogsExporterBehavior(
            endpoint = endpoint,
            timeout = timeout,
            headers = headers,
        )
    }
}
