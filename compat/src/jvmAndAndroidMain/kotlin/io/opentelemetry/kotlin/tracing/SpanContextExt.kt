package io.opentelemetry.kotlin.tracing

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaImmutableSpanContext
import io.opentelemetry.kotlin.aliases.OtelJavaSpanContext
import io.opentelemetry.kotlin.tracing.ext.toOtelJavaTraceFlags
import io.opentelemetry.kotlin.tracing.ext.toOtelJavaTraceState

/**
 * Copies an opentelemetry-kotlin [SpanContext] into an opentelemetry-java span context, keeping
 * its trace flags, trace state and remote flag.
 */
@ExperimentalApi
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
