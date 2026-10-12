package io.opentelemetry.kotlin.tracing

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.propagation.utils.encodeHex
import io.opentelemetry.kotlin.propagation.utils.isValidSpanIdBytes
import io.opentelemetry.kotlin.propagation.utils.isValidTraceIdBytes

@OptIn(ExperimentalApi::class)
internal class SpanContextImpl(
    traceIdBytes: ByteArray,
    spanIdBytes: ByteArray,
    override val traceFlags: TraceFlags,
    override val isRemote: Boolean,
    override val traceState: TraceState,
) : SpanContext {

    private val traceIdData: ByteArray = traceIdBytes.copyOf()
    private val spanIdData: ByteArray = spanIdBytes.copyOf()

    override val traceIdBytes: ByteArray
        get() = traceIdData.copyOf()

    override val spanIdBytes: ByteArray
        get() = spanIdData.copyOf()

    override val isValid: Boolean =
        traceIdData.isValidTraceIdBytes() && spanIdData.isValidSpanIdBytes()

    override val traceId: String by lazy {
        traceIdData.encodeHex()
    }
    override val spanId: String by lazy {
        spanIdData.encodeHex()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other !is SpanContextImpl) {
            return false
        }
        return traceIdData.contentEquals(other.traceIdData) &&
            spanIdData.contentEquals(other.spanIdData) &&
            traceFlags == other.traceFlags &&
            isRemote == other.isRemote &&
            traceState == other.traceState
    }

    override fun hashCode(): Int {
        var result = traceIdData.contentHashCode()
        result = 31 * result + spanIdData.contentHashCode()
        result = 31 * result + traceFlags.hashCode()
        result = 31 * result + isRemote.hashCode()
        result = 31 * result + traceState.hashCode()
        return result
    }

    override fun toString(): String =
        "SpanContextImpl(traceId=$traceId, spanId=$spanId, traceFlags=$traceFlags, " +
            "isRemote=$isRemote, traceState=$traceState)"
}
