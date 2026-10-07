package io.opentelemetry.kotlin.propagation

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.factory.SpanFactory
import io.opentelemetry.kotlin.propagation.utils.W3CTraceStateCodec
import io.opentelemetry.kotlin.tracing.createSpanContext

/**
 * W3C Trace Context HTTP header propagator.
 *
 * https://www.w3.org/TR/trace-context/
 */
@OptIn(ExperimentalApi::class)
public class W3CTraceContextPropagator(
    private val spanFactory: SpanFactory,
) : TextMapPropagator {

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
            isSampled = spanContext.traceFlags.isSampled,
            isRandom = spanContext.traceFlags.isRandom,
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
        val rawTracestate = getter.get(carrier, TRACESTATE)

        val spanContext = createSpanContext(parsed.traceId, parsed.spanId) {
            isSampled = parsed.isSampled
            isRandom = parsed.isRandom
            isRemote = true
            if (rawTracestate != null) {
                // preserves header order and drops invalid entries
                traceState {
                    W3CTraceStateCodec.decode(rawTracestate).forEach { (key, value) -> put(key, value) }
                }
            }
        }
        if (!spanContext.isValid) {
            return context
        }
        return context.storeSpan(spanFactory.fromSpanContext(spanContext))
    }

    private companion object {
        const val TRACEPARENT = "traceparent"
        const val TRACESTATE = "tracestate"
        val FIELDS = listOf(TRACEPARENT, TRACESTATE)
    }
}
