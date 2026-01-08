package io.opentelemetry.kotlin.tracing

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.tracing.model.SpanContext
import io.opentelemetry.kotlin.tracing.model.TraceFlags
import io.opentelemetry.kotlin.tracing.model.TraceState

@ExperimentalApi
internal object NoopSpanContext : SpanContext {
    override val traceId: String = ""
    override val spanId: String = ""
    private val empty = ByteArray(0)
    override val traceIdBytes: ByteArray = empty
    override val spanIdBytes: ByteArray = empty
    override val traceFlags: TraceFlags = NoopTraceFlags
    override val isValid: Boolean = false
    override val isRemote: Boolean = false
    override val traceState: TraceState = NoopTraceState
}
