package io.opentelemetry.kotlin.config.yaml.tracing

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.behavior.ConsoleExporterBehavior
import io.opentelemetry.kotlin.behavior.SpanExporterBehavior
import io.opentelemetry.kotlin.config.schema.model.SpanExporter

/**
 * Maps `tracer_provider.processors.exporter` onto its behavior.
 */
@ExperimentalApi
fun SpanExporter.toBehavior(): SpanExporterBehavior? {
    if (console == null && otlpHttp == null) {
        return null
    }
    return SpanExporterBehavior(
        console = console?.let { ConsoleExporterBehavior() },
        http = otlpHttp?.toBehavior()
    )
}
