package io.opentelemetry.kotlin.factory

import io.opentelemetry.kotlin.tracing.SpanContext
import io.opentelemetry.kotlin.tracing.SpanContextImpl
import io.opentelemetry.kotlin.tracing.TraceFlags
import io.opentelemetry.kotlin.tracing.TraceState

private val INVALID_TRACE_ID_BYTES = ByteArray(TRACE_ID_BYTES)
private val INVALID_SPAN_ID_BYTES = ByteArray(SPAN_ID_BYTES)

public class SpanContextFactoryImpl(
    private val traceFlagsFactory: TraceFlagsFactory = TraceFlagsFactoryImpl(),
    private val traceStateFactory: TraceStateFactory = TraceStateFactoryImpl()
) : SpanContextFactory {

    override val invalid: SpanContext by lazy {
        SpanContextImpl(
            traceIdBytes = INVALID_TRACE_ID_BYTES,
            spanIdBytes = INVALID_SPAN_ID_BYTES,
            traceFlags = traceFlagsFactory.default,
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
    ): SpanContext = create(
        traceId.hexToByteArray(),
        spanId.hexToByteArray(),
        traceFlags,
        traceState,
        isRemote,
    )

    override fun create(
        traceIdBytes: ByteArray,
        spanIdBytes: ByteArray,
        traceFlags: TraceFlags,
        traceState: TraceState,
        isRemote: Boolean,
    ): SpanContext = SpanContextImpl(
        traceIdBytes = if (traceIdBytes.isValidTraceIdBytes()) {
            traceIdBytes
        } else {
            INVALID_TRACE_ID_BYTES
        },
        spanIdBytes = if (spanIdBytes.isValidSpanIdBytes()) {
            spanIdBytes
        } else {
            INVALID_SPAN_ID_BYTES
        },
        traceFlags = traceFlags,
        isRemote = isRemote,
        traceState = traceState,
    )
}
