package io.opentelemetry.kotlin.config.envar.tracing

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.behavior.ConsoleExporterBehavior
import io.opentelemetry.kotlin.behavior.SpanProcessorBehavior
import io.opentelemetry.kotlin.config.envar.EnvVarReader

/**
 * Maps `OTEL_TRACES_EXPORTER` onto processor behavior. Console is the only exporter this mapper
 * understands. Unrecognized exporter names are ignored (and reported via [onWarning]).
 *
 * https://opentelemetry.io/docs/specs/otel/configuration/sdk-environment-variables/#exporter-selection
 */
@ExperimentalApi
class TracesExporterEnvVars(
    private val reader: EnvVarReader,
    private val onWarning: (String) -> Unit = {},
) {
    fun toBehavior(): SpanProcessorBehavior? {
        val name = reader.readString(EXPORTER)?.takeIf { it.isNotEmpty() } ?: return null
        return when (name.lowercase()) {
            CONSOLE -> SpanProcessorBehavior(console = ConsoleExporterBehavior())
            OTLP, LOGGING, NONE, OTLP_STDOUT -> null
            else -> null.also { onWarning("Unknown OTEL_TRACES_EXPORTER value '$name'; ignoring") }
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
