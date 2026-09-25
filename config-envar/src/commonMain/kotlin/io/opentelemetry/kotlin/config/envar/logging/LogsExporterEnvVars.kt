package io.opentelemetry.kotlin.config.envar.logging

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.behavior.ConsoleExporterBehavior
import io.opentelemetry.kotlin.behavior.LogRecordProcessorBehavior
import io.opentelemetry.kotlin.behavior.OtlpHttpExporterBehavior
import io.opentelemetry.kotlin.config.envar.OpenTelemetryEnvVars
import io.opentelemetry.kotlin.config.envar.reader.EnvVarReadResult.Invalid
import io.opentelemetry.kotlin.config.envar.reader.EnvVarReadResult.Value
import io.opentelemetry.kotlin.config.envar.reader.EnvVarReadWarning
import io.opentelemetry.kotlin.config.envar.reader.ReportingEnvVarReader

/**
 * Maps `OTEL_LOGS_EXPORTER` onto processor behavior.
 * Unrecognized exporter names are ignored and reported as warnings.
 *
 * https://opentelemetry.io/docs/specs/otel/configuration/sdk-environment-variables/#exporter-selection
 */
@ExperimentalApi
class LogsExporterEnvVars(
    private val reader: ReportingEnvVarReader,
) {
    fun toBehavior(): LogRecordProcessorBehavior? = reader.readStringAndTransform(EXPORTER) { name ->
        // TODO: Add support to multiple exporters being in use simultaneously once we have the LogRecordProcessor
        //  fully implemented.
        when (name.lowercase()) {
            CONSOLE -> Value(LogRecordProcessorBehavior(console = ConsoleExporterBehavior()))
            OTLP -> Value(
                LogRecordProcessorBehavior(
                    http = OtlpHttpExporterBehavior(
                        endpoint = reader.readString(OTLP_LOGS_ENDPOINT)
                            ?: reader.readString(OpenTelemetryEnvVars.OTLP_ENDPOINT),
                        timeout = reader.readNonNegativeLong(OTLP_LOGS_TIMEOUT)
                            ?: reader.readNonNegativeLong(OpenTelemetryEnvVars.OTLP_TIMEOUT)
                    )
                )
            )
            LOGGING, NONE, OTLP_STDOUT -> Value(null)
            else -> Invalid(EnvVarReadWarning(EXPORTER, "Unknown value '$name'; ignoring"))
        }
    }

    internal companion object {
        const val EXPORTER = "OTEL_LOGS_EXPORTER"
        const val OTLP_LOGS_ENDPOINT = "OTEL_EXPORTER_OTLP_LOGS_ENDPOINT"
        const val OTLP_LOGS_TIMEOUT = "OTEL_EXPORTER_OTLP_LOGS_TIMEOUT"

        const val CONSOLE = "console"
        const val OTLP = "otlp"
        const val LOGGING = "logging"
        const val NONE = "none"
        const val OTLP_STDOUT = "otlp/stdout"
    }
}
