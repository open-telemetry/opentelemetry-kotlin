package io.opentelemetry.kotlin.tracing.model

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaSpanContext
import io.opentelemetry.kotlin.factory.hexToByteArray

@OptIn(ExperimentalApi::class)
internal class SpanContextAdapter(
    val impl: OtelJavaSpanContext
) : SpanContext {
    override val traceId: String = impl.traceId
    override val traceIdBytes: ByteArray = impl.traceId.hexToByteArray()
    override val spanId: String = impl.spanId
    override val spanIdBytes: ByteArray = impl.spanId.hexToByteArray()
    override val traceFlags: TraceFlags = TraceFlagsAdapter(impl.traceFlags)
    override val isValid: Boolean = impl.isValid
    override val isRemote: Boolean = impl.isRemote
    override val traceState: TraceState = TraceStateAdapter(impl.traceState)
}
