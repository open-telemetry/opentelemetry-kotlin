package io.opentelemetry.kotlin.tracing.export

import io.opentelemetry.kotlin.aliases.OtelJavaSpanExporter

/**
 * Converts an opentelemetry-java span exporter to an opentelemetry-kotlin span exporter.
 * This is useful if you wish to use an existing Java exporter whilst using opentelemetry-kotlin.
 */
public fun OtelJavaSpanExporter.toOtelKotlinSpanExporter(): SpanExporter = SpanExporterAdapter(this)
