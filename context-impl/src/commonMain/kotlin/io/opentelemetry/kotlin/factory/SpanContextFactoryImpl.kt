package io.opentelemetry.kotlin.factory

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.tracing.SpanContext
import io.opentelemetry.kotlin.tracing.TraceFlags
import io.opentelemetry.kotlin.tracing.TraceState
import io.opentelemetry.kotlin.tracing.createSpanContext

@OptIn(ExperimentalApi::class)
public class SpanContextFactoryImpl(
    traceFlagsFactory: TraceFlagsFactory,
    traceStateFactory: TraceStateFactory,
) : SpanContextFactory {

    override val invalid: SpanContext by lazy {
        create(
            traceIdBytes = ByteArray(0),
            spanIdBytes = ByteArray(0),
            traceFlags = traceFlagsFactory.default,
            traceState = traceStateFactory.default,
            isRemote = false,
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
    ): SpanContext {
        val entries = traceState.asMap()
        return createSpanContext(traceIdBytes, spanIdBytes) {
            isSampled = traceFlags.isSampled
            isRandom = traceFlags.isRandom
            this.isRemote = isRemote
            traceState {
                entries.forEach { (key, value) -> put(key, value) }
            }
        }
    }
}
