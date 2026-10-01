package io.opentelemetry.kotlin.tracing

import io.opentelemetry.kotlin.ExperimentalApi

/**
 * Creates a [SpanContext] without requiring an [io.opentelemetry.kotlin.OpenTelemetry] instance.
 *
 * A valid [traceId] is a 32-character hex string (16 bytes) with at least one non-zero byte.
 * A valid [spanId] is a 16-character hex string (8 bytes) with at least one non-zero byte.
 * Invalid IDs are replaced with all zeros and the returned SpanContext will have isValid = false.
 *
 * [TraceFlags] and [TraceState] can be configured inside the [action] DSL block. Omitting them
 * results in the default TraceFlags (no flags set) and an empty TraceState.
 *
 * https://opentelemetry.io/docs/specs/otel/trace/api/#spancontext
 */
@ExperimentalApi
public fun createSpanContext(
    traceId: String,
    spanId: String,
    isRemote: Boolean = false,
    action: SpanContextCreationAction.() -> Unit = {},
): SpanContext = TODO()

/**
 * Creates a [SpanContext] without requiring an [io.opentelemetry.kotlin.OpenTelemetry] instance.
 *
 * A valid [traceIdBytes] is 16 bytes with at least one non-zero byte.
 * A valid [spanIdBytes] is 8 bytes with at least one non-zero byte.
 * Invalid IDs are replaced with all zeros and the returned SpanContext will have isValid = false.
 *
 * [TraceFlags] and [TraceState] can be configured inside the [action] DSL block. Omitting them
 * results in the default TraceFlags (no flags set) and an empty TraceState.
 *
 * https://opentelemetry.io/docs/specs/otel/trace/api/#spancontext
 */
@ExperimentalApi
public fun createSpanContext(
    traceIdBytes: ByteArray,
    spanIdBytes: ByteArray,
    isRemote: Boolean = false,
    action: SpanContextCreationAction.() -> Unit = {},
): SpanContext = TODO()

/**
 * Returns an invalid [SpanContext]. This has an all-zero trace ID and span ID, the default
 * [TraceFlags], an empty [TraceState], and is not remote.
 *
 * https://opentelemetry.io/docs/specs/otel/trace/api/#isvalid
 */
@ExperimentalApi
public fun invalidSpanContext(): SpanContext = TODO()
