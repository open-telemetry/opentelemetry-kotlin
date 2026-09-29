package io.opentelemetry.kotlin.tracing.ext

import io.opentelemetry.kotlin.aliases.OtelJavaSpanContext
import io.opentelemetry.kotlin.factory.DefaultSpanContextFactory
import io.opentelemetry.kotlin.factory.DefaultTraceFlagsFactory
import io.opentelemetry.kotlin.tracing.SpanContext

/**
 * Copies an opentelemetry-java span context into the Kotlin [SpanContext] implementation. Any
 * trace state entry that the Kotlin implementation rejects is dropped.
 */
public fun OtelJavaSpanContext.toOtelKotlinSpanContext(): SpanContext =
    DefaultSpanContextFactory.create(
        traceId = traceId,
        spanId = spanId,
        traceFlags = DefaultTraceFlagsFactory.fromHex(traceFlags.asHex()),
        traceState = traceState.toOtelKotlinTraceState(),
        isRemote = isRemote,
    )
