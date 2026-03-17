package io.opentelemetry.kotlin.factory

import io.opentelemetry.kotlin.tracing.NoopSpanContext
import io.opentelemetry.kotlin.tracing.model.SpanContext
import io.opentelemetry.kotlin.tracing.model.TraceFlags
import io.opentelemetry.kotlin.tracing.model.TraceState

internal object NoopSpanContextFactory : SpanContextFactory {

    override val invalid: SpanContext = NoopSpanContext

    override fun create(
        traceId: String,
        spanId: String,
        traceFlags: TraceFlags,
        traceState: TraceState,
        isRemote: Boolean,
    ): SpanContext = NoopSpanContext

    override fun create(
        traceIdBytes: ByteArray,
        spanIdBytes: ByteArray,
        traceFlags: TraceFlags,
        traceState: TraceState,
        isRemote: Boolean,
    ): SpanContext = NoopSpanContext
}
