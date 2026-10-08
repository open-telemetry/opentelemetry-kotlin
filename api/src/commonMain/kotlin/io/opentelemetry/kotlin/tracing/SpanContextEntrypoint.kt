package io.opentelemetry.kotlin.tracing

import io.opentelemetry.kotlin.ExperimentalApi

/**
 * Returns the invalid [SpanContext], which has all-zero IDs.
 *
 * This does not require an [io.opentelemetry.kotlin.OpenTelemetry] instance.
 *
 * https://opentelemetry.io/docs/specs/otel/trace/api/#spancontext
 */
@OptIn(ExperimentalApi::class)
internal fun createInvalidSpanContext(): SpanContext = PlaceholderSpanContext

/**
 * Creates a [SpanContext] from hex-encoded IDs. Optional properties are configured inside the
 * [action] DSL block.
 *
 * This does not require an [io.opentelemetry.kotlin.OpenTelemetry] instance.
 *
 * https://opentelemetry.io/docs/specs/otel/trace/api/#spancontext
 */
@OptIn(ExperimentalApi::class)
@Suppress("UnusedParameter")
internal fun createSpanContext(
    traceId: String,
    spanId: String,
    action: SpanContextCreationAction.() -> Unit = {},
): SpanContext = PlaceholderSpanContext

/**
 * Creates a [SpanContext] from ID bytes. Optional properties are configured inside the
 * [action] DSL block.
 *
 * This does not require an [io.opentelemetry.kotlin.OpenTelemetry] instance.
 *
 * https://opentelemetry.io/docs/specs/otel/trace/api/#spancontext
 */
@OptIn(ExperimentalApi::class)
@Suppress("UnusedParameter")
internal fun createSpanContext(
    traceIdBytes: ByteArray,
    spanIdBytes: ByteArray,
    action: SpanContextCreationAction.() -> Unit = {},
): SpanContext = PlaceholderSpanContext
