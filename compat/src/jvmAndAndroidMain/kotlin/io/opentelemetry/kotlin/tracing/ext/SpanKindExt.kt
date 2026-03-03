package io.opentelemetry.kotlin.tracing.ext

import io.opentelemetry.kotlin.aliases.OtelJavaSpanKind
import io.opentelemetry.kotlin.tracing.model.SpanKind

internal fun SpanKind.toOtelJavaSpanKind(): OtelJavaSpanKind = when (this) {
    SpanKind.INTERNAL -> OtelJavaSpanKind.INTERNAL
    SpanKind.CLIENT -> OtelJavaSpanKind.CLIENT
    SpanKind.SERVER -> OtelJavaSpanKind.SERVER
    SpanKind.PRODUCER -> OtelJavaSpanKind.PRODUCER
    SpanKind.CONSUMER -> OtelJavaSpanKind.CONSUMER
}
