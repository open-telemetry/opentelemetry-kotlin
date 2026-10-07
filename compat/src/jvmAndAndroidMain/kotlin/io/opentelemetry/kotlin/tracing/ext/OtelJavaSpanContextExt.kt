package io.opentelemetry.kotlin.tracing.ext

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaSpanContext
import io.opentelemetry.kotlin.propagation.utils.TRACE_FLAG_RANDOM
import io.opentelemetry.kotlin.propagation.utils.TRACE_FLAG_SAMPLED
import io.opentelemetry.kotlin.propagation.utils.decodeTraceFlagsOrZero
import io.opentelemetry.kotlin.tracing.SpanContext
import io.opentelemetry.kotlin.tracing.createSpanContext

/**
 * Copies an opentelemetry-java span context into the Kotlin [SpanContext] implementation. Any
 * trace state entry that the Kotlin implementation rejects is dropped.
 */
@OptIn(ExperimentalApi::class)
public fun OtelJavaSpanContext.toOtelKotlinSpanContext(): SpanContext {
    val flags = traceFlags.asHex().decodeTraceFlagsOrZero()
    return createSpanContext(traceId, spanId) {
        isSampled = (flags and TRACE_FLAG_SAMPLED) != 0
        isRandom = (flags and TRACE_FLAG_RANDOM) != 0
        isRemote = this@toOtelKotlinSpanContext.isRemote
        traceState {
            this@toOtelKotlinSpanContext.traceState.forEach { key, value -> put(key, value) }
        }
    }
}
