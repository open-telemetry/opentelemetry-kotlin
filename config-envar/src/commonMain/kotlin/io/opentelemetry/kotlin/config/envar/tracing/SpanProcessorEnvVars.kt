package io.opentelemetry.kotlin.config.envar.tracing

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.behavior.SpanProcessorBehavior
import io.opentelemetry.kotlin.config.envar.reader.ReportingEnvVarReader

/**
 * Maps `OTEL_TRACES_EXPORTER` onto processor behavior.
 * Unrecognized exporter names are ignored and reported as warnings.
 *
 * https://opentelemetry.io/docs/specs/otel/configuration/sdk-environment-variables/#exporter-selection
 */
@ExperimentalApi
class SpanProcessorEnvVars(
    private val reader: ReportingEnvVarReader,
) {
    fun toBehavior(): SpanProcessorBehavior = SpanProcessorBehavior(
        batch = BatchSpanProcessorEnvVars(reader).toBehavior()
    )
}
