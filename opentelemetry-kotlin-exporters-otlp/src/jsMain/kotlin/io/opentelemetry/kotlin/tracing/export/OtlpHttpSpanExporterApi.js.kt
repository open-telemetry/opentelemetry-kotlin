package io.opentelemetry.kotlin.tracing.export

import io.opentelemetry.kotlin.ExperimentalApi

@OptIn(ExperimentalApi::class)
public actual fun createOtlpHttpSpanExporter(baseUrl: String): SpanExporter {
    throw UnsupportedOperationException()
}
