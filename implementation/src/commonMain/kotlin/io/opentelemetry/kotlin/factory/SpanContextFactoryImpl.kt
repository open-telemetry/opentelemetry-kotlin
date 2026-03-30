package io.opentelemetry.kotlin.factory

import io.opentelemetry.kotlin.tracing.SpanContext
import io.opentelemetry.kotlin.tracing.SpanContextImpl
import io.opentelemetry.kotlin.tracing.TraceFlags
import io.opentelemetry.kotlin.tracing.TraceState

internal class SpanContextFactoryImpl(
    private val idGenerator: IdGenerator,
    private val traceFlagsFactory: TraceFlagsFactory = TraceFlagsFactoryImpl(),
    private val traceStateFactory: TraceStateFactory = TraceStateFactoryImpl()
) : SpanContextFactory {

    override val invalid: SpanContext by lazy {
        SpanContextImpl(
            traceIdBytes = idGenerator.invalidTraceId,
            spanIdBytes = idGenerator.invalidSpanId,
            traceFlags = traceFlagsFactory.default,
            isValid = false,
            isRemote = false,
            traceState = traceStateFactory.default
        )
    }

    override fun create(
        traceId: String,
        spanId: String,
        traceFlags: TraceFlags,
        traceState: TraceState,
        isRemote: Boolean,
    ): SpanContext {
        val isValidTraceId = isValidTraceId(traceId)
        val isValidSpanId = isValidSpanId(spanId)

        return SpanContextImpl(
            traceIdBytes = when {
                isValidTraceId -> traceId.hexToByteArray()
                else -> idGenerator.invalidTraceId
            },
            spanIdBytes = when {
                isValidSpanId -> spanId.hexToByteArray()
                else -> idGenerator.invalidSpanId
            },
            traceFlags = traceFlags,
            isValid = isValidTraceId && isValidSpanId,
            isRemote = isRemote,
            traceState = traceState
        )
    }

    override fun create(
        traceIdBytes: ByteArray,
        spanIdBytes: ByteArray,
        traceFlags: TraceFlags,
        traceState: TraceState,
        isRemote: Boolean,
    ): SpanContext = create(
        traceIdBytes.toHexString(),
        spanIdBytes.toHexString(),
        traceFlags,
        traceState,
        isRemote,
    )

    private fun isValidTraceId(traceId: String): Boolean {
        // Must be 32 hex characters (16 bytes)
        if (traceId.length != 32) {
            return false
        }

        // Must be valid hex
        if (!traceId.isValidHex()) {
            return false
        }

        // Must have at least one non-zero byte (not all zeros)
        return traceId != "00000000000000000000000000000000"
    }

    private fun isValidSpanId(spanId: String): Boolean {
        // Must be 16 hex characters (8 bytes)
        if (spanId.length != 16) {
            return false
        }

        // Must be valid hex
        if (!spanId.isValidHex()) {
            return false
        }

        // Must have at least one non-zero byte (not all zeros)
        return spanId != "0000000000000000"
    }
}
