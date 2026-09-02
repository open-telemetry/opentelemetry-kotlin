package io.opentelemetry.kotlin.framework.serialization.conversion

import io.opentelemetry.kotlin.framework.serialization.SerializableSpanContext
import io.opentelemetry.kotlin.tracing.SpanContext
import io.opentelemetry.kotlin.tracing.model.hex

fun SpanContext.toSerializable() =
    SerializableSpanContext(
        traceId = traceId,
        spanId = spanId,
        traceFlags = traceFlags.hex,
        traceState = traceState.asMap(),
    )
