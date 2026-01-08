package io.opentelemetry.kotlin.factory

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaSpanContext
import io.opentelemetry.kotlin.tracing.SpanContextImpl
import io.opentelemetry.kotlin.tracing.ext.toOtelJavaTraceFlags
import io.opentelemetry.kotlin.tracing.ext.toOtelJavaTraceState
import io.opentelemetry.kotlin.tracing.model.SpanContext
import io.opentelemetry.kotlin.tracing.model.SpanContextAdapter
import io.opentelemetry.kotlin.tracing.model.TraceFlags
import io.opentelemetry.kotlin.tracing.model.TraceFlagsAdapter
import io.opentelemetry.kotlin.tracing.model.TraceState
import io.opentelemetry.kotlin.tracing.model.TraceStateAdapter

@OptIn(ExperimentalApi::class)
internal class CompatSpanContextFactory : SpanContextFactory {

    override val invalid: SpanContext by lazy {
        val impl: OtelJavaSpanContext = OtelJavaSpanContext.getInvalid()

        SpanContextImpl(
            traceIdBytes = impl.traceId.hexToByteArray(),
            spanIdBytes = impl.spanId.hexToByteArray(),
            traceFlags = TraceFlagsAdapter(impl.traceFlags),
            isValid = impl.isValid,
            isRemote = impl.isRemote,
            traceState = TraceStateAdapter(impl.traceState)
        )
    }

    override fun create(
        traceId: String,
        spanId: String,
        traceFlags: TraceFlags,
        traceState: TraceState
    ): SpanContext = SpanContextAdapter(
        OtelJavaSpanContext.create(
            traceId,
            spanId,
            traceFlags.toOtelJavaTraceFlags(),
            traceState.toOtelJavaTraceState()
        )
    )

    override fun create(
        traceIdBytes: ByteArray,
        spanIdBytes: ByteArray,
        traceFlags: TraceFlags,
        traceState: TraceState
    ): SpanContext = SpanContextAdapter(
        OtelJavaSpanContext.create(
            traceIdBytes.toHexString(),
            spanIdBytes.toHexString(),
            traceFlags.toOtelJavaTraceFlags(),
            traceState.toOtelJavaTraceState()
        )
    )
}
