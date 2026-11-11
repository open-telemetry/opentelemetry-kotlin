package io.opentelemetry.kotlin.config.envar.logging

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.behavior.LogRecordProcessorBehavior
import io.opentelemetry.kotlin.config.envar.reader.ReportingEnvVarReader

/**
 * Maps `OTEL_LOGS_EXPORTER` onto processor behavior.
 * Unrecognized exporter names are ignored and reported as warnings.
 *
 * https://opentelemetry.io/docs/specs/otel/configuration/sdk-environment-variables/#exporter-selection
 */
@OptIn(ExperimentalApi::class)
class LogRecordProcessorEnvVars(
    private val reader: ReportingEnvVarReader,
) {
    fun toBehavior(): LogRecordProcessorBehavior? {
        return LogRecordProcessorBehavior(
            batch = BatchLogRecordProcessorEnvVars(reader).toBehavior(),
        )
    }
}
