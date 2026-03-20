package io.opentelemetry.kotlin.tracing

import io.opentelemetry.kotlin.factory.toHexString

class SpanContextImpl(
    override val traceIdBytes: ByteArray,
    override val spanIdBytes: ByteArray,
    override val traceFlags: TraceFlags,
    override val isValid: Boolean,
    override val isRemote: Boolean,
    override val traceState: TraceState,
) : SpanContext {

    override val traceId: String by lazy {
        traceIdBytes.toHexString()
    }
    override val spanId: String by lazy {
        spanIdBytes.toHexString()
    }
}
