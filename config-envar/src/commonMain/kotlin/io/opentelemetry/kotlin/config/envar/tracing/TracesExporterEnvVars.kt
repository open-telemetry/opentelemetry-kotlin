package io.opentelemetry.kotlin.config.envar.tracing

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.behavior.SpanExporterBehavior
import io.opentelemetry.kotlin.config.envar.reader.EnvVarReadResult.Invalid
import io.opentelemetry.kotlin.config.envar.reader.EnvVarReadResult.Value
import io.opentelemetry.kotlin.config.envar.reader.EnvVarReadWarning
import io.opentelemetry.kotlin.config.envar.reader.ReportingEnvVarReader

/**
 * Maps `OTEL_TRACES_EXPORTER` onto exporter behavior. Console is the only exporter this mapper
 * understands. Unrecognized exporter names are ignored and reported as warnings.
 *
 * https://opentelemetry.io/docs/specs/otel/configuration/sdk-environment-variables/#exporter-selection
 */
@ExperimentalApi
class TracesExporterEnvVars(private val reader: ReportingEnvVarReader) {

    fun toBehavior(): SpanExporterBehavior? = reader.readStringAndTransform(EXPORTER) { name ->
        when (name.lowercase()) {
            CONSOLE -> Value(SpanExporterBehavior.Console)
            OTLP, LOGGING, NONE, OTLP_STDOUT -> Value(null)
            else -> Invalid(EnvVarReadWarning(EXPORTER, "Unknown value '$name'; ignoring"))
        }
    }

    private companion object {
        const val EXPORTER = "OTEL_TRACES_EXPORTER"
        const val CONSOLE = "console"
        const val OTLP = "otlp"
        const val LOGGING = "logging"
        const val NONE = "none"
        const val OTLP_STDOUT = "otlp/stdout"
    }
}
