package io.opentelemetry.kotlin.tracing.ext

import io.opentelemetry.kotlin.aliases.OtelJavaSpanContext
import io.opentelemetry.kotlin.tracing.SpanContext
import io.opentelemetry.kotlin.tracing.createSpanContext

private const val SAMPLED_BIT = 0b01
private const val RANDOM_BIT = 0b10

/**
 * Copies an opentelemetry-java span context into the Kotlin [SpanContext] implementation. Any
 * trace state entry that the Kotlin implementation rejects is dropped.
 */
public fun OtelJavaSpanContext.toOtelKotlinSpanContext(): SpanContext {
    val flags = traceFlags.asByte().toInt()
    return createSpanContext(traceId, spanId) {
        isSampled = (flags and SAMPLED_BIT) != 0
        isRandom = (flags and RANDOM_BIT) != 0
        isRemote = this@toOtelKotlinSpanContext.isRemote
        traceState {
            this@toOtelKotlinSpanContext.traceState.forEach { key, value -> put(key, value) }
        }
    }
}
