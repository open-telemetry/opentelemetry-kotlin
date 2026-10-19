package io.opentelemetry.kotlin.config.yaml.logging

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.behavior.ConsoleExporterBehavior
import io.opentelemetry.kotlin.behavior.LogRecordExporterBehavior
import io.opentelemetry.kotlin.config.schema.model.LogRecordExporter

/**
 * Maps `logger_provider.processors.exporter` onto its behavior.
 */
@ExperimentalApi
fun LogRecordExporter.toBehavior(): LogRecordExporterBehavior? {
    if (console == null && otlpHttp == null) {
        return null
    }
    return LogRecordExporterBehavior(
        console = console?.let { ConsoleExporterBehavior() },
        http = otlpHttp?.toBehavior()
    )
}
