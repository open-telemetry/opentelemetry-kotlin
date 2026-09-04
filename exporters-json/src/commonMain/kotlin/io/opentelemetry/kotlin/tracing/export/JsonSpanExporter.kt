package io.opentelemetry.kotlin.tracing.export

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.export.JsonExporter

@ExperimentalApi
abstract class JsonSpanExporter : JsonExporter(), SpanExporter