package io.opentelemetry.kotlin.factory

import io.opentelemetry.kotlin.tracing.FakeSpanContext
import io.opentelemetry.kotlin.tracing.SpanContext
import io.opentelemetry.kotlin.tracing.TraceFlags
import io.opentelemetry.kotlin.tracing.TraceState

class FakeSpanContextFactory : SpanContextFactory {

    override val invalid: SpanContext = FakeSpanContext.INVALID

    override fun create(
        traceId: String,
        spanId: String,
        traceFlags: TraceFlags,
        traceState: TraceState,
        isRemote: Boolean,
    ): SpanContext = FakeSpanContext(
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
    ): SpanContext = FakeSpanContext(
        traceIdBytes,
        spanIdBytes,
        traceFlags,
        traceState,
        isRemote,
    )
}
