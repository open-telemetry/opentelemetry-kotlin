package io.opentelemetry.kotlin.tracing.export

import io.opentelemetry.kotlin.ExperimentalApi

/**
 * A span exporter that returns telemetry in JSON Format.
 */
@ExperimentalApi
public interface JsonSpanExporter : SpanExporter
