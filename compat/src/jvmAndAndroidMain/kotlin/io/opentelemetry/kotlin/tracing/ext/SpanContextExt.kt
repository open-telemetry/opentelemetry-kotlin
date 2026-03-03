package io.opentelemetry.kotlin.tracing.ext

import io.opentelemetry.kotlin.aliases.OtelJavaImmutableSpanContext
import io.opentelemetry.kotlin.aliases.OtelJavaSpanContext
import io.opentelemetry.kotlin.tracing.model.SpanContext

public fun SpanContext.toOtelJavaSpanContext(): OtelJavaSpanContext {
    return OtelJavaImmutableSpanContext.create(
        traceId,
        spanId,
        traceFlags.toOtelJavaTraceFlags(),
        traceState.toOtelJavaTraceState(),
        isRemote,
        false
    )
}
