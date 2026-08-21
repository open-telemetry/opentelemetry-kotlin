package io.opentelemetry.kotlin.tracing

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.factory.toHexString

@ExperimentalApi
internal object NoopSpanContext : SpanContext {
    override val traceIdBytes: ByteArray = ByteArray(16)
    override val spanIdBytes: ByteArray = ByteArray(8)
    override val traceId: String = traceIdBytes.toHexString()
    override val spanId: String = spanIdBytes.toHexString()
    override val traceFlags: TraceFlags = NoopTraceFlags
    override val isValid: Boolean = false
    override val isRemote: Boolean = false
    override val traceState: TraceState = NoopTraceState
}
