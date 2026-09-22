package io.opentelemetry.kotlin.config.envar.logging

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.behavior.ConsoleExporterBehavior
import io.opentelemetry.kotlin.behavior.LogRecordProcessorBehavior
import io.opentelemetry.kotlin.config.envar.reader.EnvVarReadResult.Invalid
import io.opentelemetry.kotlin.config.envar.reader.EnvVarReadResult.Value
import io.opentelemetry.kotlin.config.envar.reader.EnvVarReadWarning
import io.opentelemetry.kotlin.config.envar.reader.ReportingEnvVarReader

/**
 * Maps `OTEL_LOGS_EXPORTER` onto processor behavior. Console is the only exporter this mapper
 * understands. Unrecognized exporter names are ignored and reported as warnings.
 *
 * https://opentelemetry.io/docs/specs/otel/configuration/sdk-environment-variables/#exporter-selection
 */
@ExperimentalApi
class LogsExporterEnvVars(private val reader: ReportingEnvVarReader) {

    fun toBehavior(): LogRecordProcessorBehavior? = reader.readStringAndTransform(EXPORTER) { name ->
        when (name.lowercase()) {
            CONSOLE -> Value(LogRecordProcessorBehavior(console = ConsoleExporterBehavior()))
            OTLP, LOGGING, NONE, OTLP_STDOUT -> Value(null)
            else -> Invalid(EnvVarReadWarning(EXPORTER, "Unknown value '$name'; ignoring"))
        }
    }

    private companion object {
        const val EXPORTER = "OTEL_LOGS_EXPORTER"
        const val CONSOLE = "console"
        const val OTLP = "otlp"
        const val LOGGING = "logging"
        const val NONE = "none"
        const val OTLP_STDOUT = "otlp/stdout"
    }
}
