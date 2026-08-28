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
            idGenerator.invalidTraceId
        },
        spanIdBytes = if (spanIdBytes.isValidSpanIdBytes()) {
            spanIdBytes
        } else {
            idGenerator.invalidSpanId
        },
        traceFlags = traceFlags,
        isRemote = isRemote,
        traceState = traceState,
    )
}
