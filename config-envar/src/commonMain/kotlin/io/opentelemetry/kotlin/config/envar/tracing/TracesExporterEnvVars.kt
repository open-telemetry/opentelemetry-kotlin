package io.opentelemetry.kotlin.config.envar.tracing

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.behavior.ConsoleExporterBehavior
import io.opentelemetry.kotlin.behavior.OtlpHttpExporterBehavior
import io.opentelemetry.kotlin.behavior.SpanProcessorBehavior
import io.opentelemetry.kotlin.config.envar.OpenTelemetryEnvVars
import io.opentelemetry.kotlin.config.envar.reader.EnvVarReadResult.Invalid
import io.opentelemetry.kotlin.config.envar.reader.EnvVarReadResult.Value
import io.opentelemetry.kotlin.config.envar.reader.EnvVarReadWarning
import io.opentelemetry.kotlin.config.envar.reader.ReportingEnvVarReader

/**
 * Maps `OTEL_TRACES_EXPORTER` onto processor behavior.
 * Unrecognized exporter names are ignored and reported as warnings.
 *
 * https://opentelemetry.io/docs/specs/otel/configuration/sdk-environment-variables/#exporter-selection
 */
@ExperimentalApi
class TracesExporterEnvVars(
    private val reader: ReportingEnvVarReader,
) {
    fun toBehavior(): SpanProcessorBehavior? = reader.readStringAndTransform(EXPORTER) { name ->
        // TODO: Add support to multiple exporters being in use simultaneously once we have the SpanProcessor
        //  fully implemented.
        when (name.lowercase()) {
            CONSOLE -> Value(SpanProcessorBehavior(console = ConsoleExporterBehavior()))
            OTLP -> Value(
                SpanProcessorBehavior(
                    http = OtlpHttpExporterBehavior(
                        endpoint = reader.readString(OTLP_TRACES_ENDPOINT)
                            ?: reader.readString(OpenTelemetryEnvVars.OTLP_ENDPOINT),
                        timeout = reader.readNonNegativeLong(OTLP_TRACES_TIMEOUT)
                            ?: reader.readNonNegativeLong(OpenTelemetryEnvVars.OTLP_TIMEOUT)
                    )
                )
            )
            LOGGING, NONE, OTLP_STDOUT -> Value(null)
            else -> Invalid(EnvVarReadWarning(EXPORTER, "Unknown value '$name'; ignoring"))
        }
    }

    internal companion object {
        const val EXPORTER = "OTEL_TRACES_EXPORTER"
        const val OTLP_TRACES_ENDPOINT = "OTEL_EXPORTER_OTLP_TRACES_ENDPOINT"
        const val OTLP_TRACES_TIMEOUT = "OTEL_EXPORTER_OTLP_TRACES_TIMEOUT"

        const val CONSOLE = "console"
        const val OTLP = "otlp"
        const val LOGGING = "logging"
        const val NONE = "none"
        const val OTLP_STDOUT = "otlp/stdout"
    }
}
