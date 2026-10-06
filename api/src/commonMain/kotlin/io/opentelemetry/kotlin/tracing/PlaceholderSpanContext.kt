package io.opentelemetry.kotlin.tracing

import io.opentelemetry.kotlin.ExperimentalApi

/**
 * Placeholder [SpanContext] returned by [createSpanContext] and [createInvalidSpanContext].
 */
@OptIn(ExperimentalApi::class)
internal object PlaceholderSpanContext : SpanContext {
    override val traceId: String = "00000000000000000000000000000000"
    override val traceIdBytes: ByteArray
        get() = ByteArray(16)
    override val spanId: String = "0000000000000000"
    override val spanIdBytes: ByteArray
        get() = ByteArray(8)
    override val traceFlags: TraceFlags = PlaceholderTraceFlags
    override val isValid: Boolean = false
    override val isRemote: Boolean = false
    override val traceState: TraceState = PlaceholderTraceState
}

/**
 * Placeholder [TraceFlags] used by [PlaceholderSpanContext].
 */
@OptIn(ExperimentalApi::class)
internal object PlaceholderTraceFlags : TraceFlags {
    override val isSampled: Boolean = false
    override val isRandom: Boolean = false
}

/**
 * Placeholder [TraceState] used by [PlaceholderSpanContext].
 */
@OptIn(ExperimentalApi::class)
internal object PlaceholderTraceState : TraceState {
    override fun get(key: String): String? = null
    override fun asMap(): Map<String, String> = emptyMap()
    override fun put(key: String, value: String): TraceState = this
    override fun remove(key: String): TraceState = this
}
