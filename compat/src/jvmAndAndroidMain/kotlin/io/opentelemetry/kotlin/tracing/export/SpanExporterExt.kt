package io.opentelemetry.kotlin.tracing.export

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaSpanExporter

/**
 * Converts an opentelemetry-java span exporter to an opentelemetry-kotlin span exporter.
 * This is useful if you wish to use an existing Java exporter whilst using opentelemetry-kotlin.
 */
@OptIn(ExperimentalApi::class)
public fun OtelJavaSpanExporter.toOtelKotlinSpanExporter(): SpanExporter = SpanExporterAdapter(this)
