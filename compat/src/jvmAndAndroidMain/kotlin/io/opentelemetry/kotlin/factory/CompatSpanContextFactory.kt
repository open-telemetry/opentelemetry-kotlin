package io.opentelemetry.kotlin.factory

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
        traceState: TraceState,
        isRemote: Boolean,
    ): SpanContext = SpanContextAdapter(
        if (isRemote) {
            OtelJavaSpanContext.createFromRemoteParent(
                traceId,
                spanId,
                traceFlags.toOtelJavaTraceFlags(),
                traceState.toOtelJavaTraceState()
            )
        } else {
            OtelJavaSpanContext.create(
                traceId,
                spanId,
                traceFlags.toOtelJavaTraceFlags(),
                traceState.toOtelJavaTraceState()
            )
        }
    )

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
}
