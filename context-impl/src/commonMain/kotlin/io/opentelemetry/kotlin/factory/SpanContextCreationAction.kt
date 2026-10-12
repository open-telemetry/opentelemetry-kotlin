package io.opentelemetry.kotlin.factory

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.tracing.SpanContext
import io.opentelemetry.kotlin.tracing.TraceFlags
import io.opentelemetry.kotlin.tracing.TraceState
import io.opentelemetry.kotlin.tracing.TraceStateCreationAction
import io.opentelemetry.kotlin.tracing.TracingDsl

/**
 * Configures the optional properties of a [SpanContext] created via [SpanContextFactory.create].
 */
@OptIn(ExperimentalApi::class)
@TracingDsl
public class SpanContextCreationAction internal constructor() {

    /**
     * Whether the trace is sampled. Defaults to false.
     */
    public var isSampled: Boolean = false

    /**
     * Whether the trace ID is random. Defaults to false.
     */
    public var isRandom: Boolean = false

    /**
     * Whether the SpanContext was propagated from a remote parent. Defaults to false.
     */
    public var isRemote: Boolean = false

    internal var traceState: TraceState = DefaultTraceStateFactory.default
        private set

    /**
     * Configures the [TraceState] entries inside the [action] DSL block. Omitting this
     * results in an empty [TraceState].
     */
    public fun traceState(action: TraceStateCreationAction.() -> Unit) {
        traceState = buildTraceState(action)
    }
}

/**
 * Creates a [SpanContext] from hex-encoded IDs, configuring optional properties inside the
 * [action] DSL block. Returns [SpanContextFactory.invalid] if [action] throws.
 */
@OptIn(ExperimentalApi::class)
public fun SpanContextFactory.create(
    traceId: String,
    spanId: String,
    action: SpanContextCreationAction.() -> Unit,
): SpanContext {
    val builder = SpanContextCreationAction()
    try {
        builder.action()
    } catch (exc: Throwable) {
        return invalid
    }
    return create(traceId, spanId, builder.traceFlags(), builder.traceState, builder.isRemote)
}

/**
 * Creates a [SpanContext] from ID bytes, configuring optional properties inside the
 * [action] DSL block. Returns [SpanContextFactory.invalid] if [action] throws.
 */
@OptIn(ExperimentalApi::class)
public fun SpanContextFactory.create(
    traceIdBytes: ByteArray,
    spanIdBytes: ByteArray,
    action: SpanContextCreationAction.() -> Unit,
): SpanContext {
    val builder = SpanContextCreationAction()
    try {
        builder.action()
    } catch (exc: Throwable) {
        return invalid
    }
    return create(traceIdBytes, spanIdBytes, builder.traceFlags(), builder.traceState, builder.isRemote)
}

@OptIn(ExperimentalApi::class)
private fun SpanContextCreationAction.traceFlags(): TraceFlags = DefaultTraceFlagsFactory.create(isSampled, isRandom)
