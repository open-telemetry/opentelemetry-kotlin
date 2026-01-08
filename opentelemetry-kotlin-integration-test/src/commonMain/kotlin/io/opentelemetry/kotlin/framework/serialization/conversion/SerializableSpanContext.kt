package io.opentelemetry.kotlin.framework.serialization.conversion

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.framework.serialization.SerializableSpanContext
import io.opentelemetry.kotlin.tracing.model.SpanContext

@OptIn(ExperimentalApi::class)
fun SpanContext.toSerializable() =
    SerializableSpanContext(
        traceId = traceId,
        spanId = spanId,
        traceFlags = traceFlags.hex,
        traceState = traceState.asMap(),
    )
