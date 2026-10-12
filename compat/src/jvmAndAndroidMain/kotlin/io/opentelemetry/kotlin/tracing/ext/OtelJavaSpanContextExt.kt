package io.opentelemetry.kotlin.tracing.ext

import io.opentelemetry.kotlin.aliases.OtelJavaSpanContext
import io.opentelemetry.kotlin.factory.DefaultTraceFlagsFactory
import io.opentelemetry.kotlin.tracing.SpanContext
import io.opentelemetry.kotlin.tracing.compat.createSpanContext

/**
 * Copies an opentelemetry-java span context into the Kotlin [SpanContext] implementation. Any
 * trace state entry that the Kotlin implementation rejects is dropped.
 */
public fun OtelJavaSpanContext.toOtelKotlinSpanContext(): SpanContext {
    val flags = DefaultTraceFlagsFactory.fromHex(traceFlags.asHex())
    return createSpanContext(traceId, spanId) {
        isSampled = flags.isSampled
        isRandom = flags.isRandom
        isRemote = this@toOtelKotlinSpanContext.isRemote
        traceState {
            this@toOtelKotlinSpanContext.traceState.forEach { key, value -> put(key, value) }
        }
    }
}
