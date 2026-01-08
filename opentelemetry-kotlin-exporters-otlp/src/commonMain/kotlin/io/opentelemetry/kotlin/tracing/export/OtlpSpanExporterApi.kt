package io.opentelemetry.kotlin.tracing.export

import io.opentelemetry.kotlin.ExperimentalApi

/**
 * Creates a span exporter that sends telemetry to the specified URL over OTLP.
 */
@ExperimentalApi
public expect fun createOtlpHttpSpanExporter(baseUrl: String): SpanExporter
