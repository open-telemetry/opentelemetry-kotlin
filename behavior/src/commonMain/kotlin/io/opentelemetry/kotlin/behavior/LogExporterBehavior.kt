package io.opentelemetry.kotlin.behavior

import io.opentelemetry.kotlin.ExperimentalApi

/**
 * Log exporter configuration.
 *
 * https://opentelemetry.io/docs/specs/otel/logs/sdk_exporters/
 */
@ExperimentalApi
sealed class LogExporterBehavior : Behavior<LogExporterBehavior> {

    /**
     * Console log exporter.
     *
     * https://opentelemetry.io/docs/specs/otel/logs/sdk_exporters/stdout/
     */
    data object Console : LogExporterBehavior() {
        override fun mergeWith(higher: LogExporterBehavior): LogExporterBehavior = higher
    }
}
