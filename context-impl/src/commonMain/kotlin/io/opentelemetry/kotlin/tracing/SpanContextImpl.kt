package io.opentelemetry.kotlin.tracing

import io.opentelemetry.kotlin.factory.toHexString
import io.opentelemetry.kotlin.propagation.utils.isValidSpanIdBytes
import io.opentelemetry.kotlin.propagation.utils.isValidTraceIdBytes

public class SpanContextImpl(
    override val traceIdBytes: ByteArray,
    override val spanIdBytes: ByteArray,
    override val traceFlags: TraceFlags,
    override val isRemote: Boolean,
    override val traceState: TraceState,
) : SpanContext {

    override val isValid: Boolean =
        traceIdBytes.isValidTraceIdBytes() && spanIdBytes.isValidSpanIdBytes()

    override val traceId: String by lazy {
        traceIdBytes.toHexString()
    }
    override val spanId: String by lazy {
        spanIdBytes.toHexString()
    }
}
