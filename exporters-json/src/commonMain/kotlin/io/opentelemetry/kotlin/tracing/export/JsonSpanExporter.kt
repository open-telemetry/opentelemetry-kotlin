package io.opentelemetry.kotlin.tracing.export

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.JsonExporter

/**
 * A span exporter that returns telemetry in JSON Format.
 */
@ExperimentalApi
abstract class JsonSpanExporter : JsonExporter(), SpanExporter
