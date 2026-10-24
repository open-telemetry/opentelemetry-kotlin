package io.opentelemetry.kotlin.propagation

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.propagation.utils.W3CTraceStateCodec
import io.opentelemetry.kotlin.tracing.createSpanContext

/**
 * W3C Trace Context HTTP header propagator.
 *
 * https://www.w3.org/TR/trace-context/
 */
@OptIn(ExperimentalApi::class)
internal object W3CTraceContextPropagator : TextMapPropagator {

    private const val TRACEPARENT = "traceparent"
    private const val TRACESTATE = "tracestate"
    private val FIELDS = listOf(TRACEPARENT, TRACESTATE)

    override fun fields(): Collection<String> = FIELDS

    override fun <T> inject(context: Context, carrier: T?, setter: TextMapSetter<T>) {
        val spanContext = context.extractSpan().spanContext
        if (!spanContext.isValid) {
            return
        }

        TraceParent.create(
            version = TraceParent.VERSION_00,
            traceId = spanContext.traceId,
            spanId = spanContext.spanId,
            traceFlags = spanContext.traceFlags,
        )?.let {
            setter.set(carrier, TRACEPARENT, it.encode())
        }

        val tracestate = TraceStateMarshaller(spanContext.traceState).encode()
        if (tracestate.isNotEmpty()) {
            setter.set(carrier, TRACESTATE, tracestate)
        }
    }

    override fun <T> extract(context: Context, carrier: T?, getter: TextMapGetter<T>): Context {
        val rawTraceparent = getter.get(carrier, TRACEPARENT) ?: return context
        val parsed = TraceParent.decode(rawTraceparent) ?: return context
        val rawTracestate = getter.getAll(carrier, TRACESTATE).joinToString(",")

        val spanContext = createSpanContext(parsed.traceId, parsed.spanId) {
            isSampled = parsed.traceFlags.isSampled
            isRandom = parsed.traceFlags.isRandom
            isRemote = true
            if (rawTracestate.isNotEmpty()) {
                traceState {
                    W3CTraceStateCodec.decode(rawTracestate).forEach { (key, value) -> put(key, value) }
                }
            }
        }
        if (!spanContext.isValid) {
            return context
        }
        return context.storeSpanContext(spanContext)
    }
}
