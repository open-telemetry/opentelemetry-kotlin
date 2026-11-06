package io.opentelemetry.kotlin.framework.serialization.conversion

import io.opentelemetry.kotlin.framework.serialization.SerializableSpanContext
import io.opentelemetry.kotlin.tracing.SpanContext
import io.opentelemetry.kotlin.tracing.hex

/**
 * Safely calls [SpanContext.traceState.asMap()], returning an empty map if the operation throws.
 *
 * This protects against external [SpanContext.traceState] implementations that may throw
 * from [asMap], which could otherwise disrupt SDK processing.
 */
private fun SpanContext.traceStateAsMapSafe(): Map<String, String> = try {
    traceState.asMap()
} catch (exc: Throwable) {
    emptyMap()
}

fun SpanContext.toSerializable() =
    SerializableSpanContext(
        traceId = traceId,
        spanId = spanId,
        traceFlags = traceFlags.hex,
        traceState = traceStateAsMapSafe(),
    )
