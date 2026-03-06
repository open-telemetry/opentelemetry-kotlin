package io.opentelemetry.kotlin.factory

import io.opentelemetry.kotlin.tracing.FakeSpanContext
import io.opentelemetry.kotlin.tracing.model.SpanContext
import io.opentelemetry.kotlin.tracing.model.TraceFlags
import io.opentelemetry.kotlin.tracing.model.TraceState

class FakeSpanContextFactory : SpanContextFactory {

    override val invalid: SpanContext = FakeSpanContext.INVALID

    override fun create(
        traceId: String,
        spanId: String,
        traceFlags: TraceFlags,
        traceState: TraceState
    ): SpanContext = FakeSpanContext(
        traceId.hexToByteArray(),
        spanId.hexToByteArray(),
        traceFlags,
        traceState
    )

    override fun create(
        traceIdBytes: ByteArray,
        spanIdBytes: ByteArray,
        traceFlags: TraceFlags,
        traceState: TraceState
    ): SpanContext = FakeSpanContext(
        traceIdBytes,
        spanIdBytes,
        traceFlags,
        traceState
    )
}
