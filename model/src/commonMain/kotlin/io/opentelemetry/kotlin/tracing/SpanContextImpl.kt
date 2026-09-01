package io.opentelemetry.kotlin.tracing

import io.opentelemetry.kotlin.factory.toHexString

private const val TRACE_ID_BYTES = 16
private const val SPAN_ID_BYTES = 8

class SpanContextImpl(
    override val traceIdBytes: ByteArray,
    override val spanIdBytes: ByteArray,
    override val traceFlags: TraceFlags,
    override val isRemote: Boolean,
    override val traceState: TraceState,
) : SpanContext {

    override val isValid: Boolean =
        isValidId(traceIdBytes, TRACE_ID_BYTES) && isValidId(spanIdBytes, SPAN_ID_BYTES)

    override val traceId: String by lazy {
        traceIdBytes.toHexString()
    }
    override val spanId: String by lazy {
        spanIdBytes.toHexString()
    }
}

private fun isValidId(id: ByteArray, expectedSize: Int): Boolean =
    id.size == expectedSize && id.any { it != 0.toByte() }
