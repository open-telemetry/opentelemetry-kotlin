package io.opentelemetry.kotlin.propagation

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.factory.SpanContextFactory
import io.opentelemetry.kotlin.factory.SpanFactory
import io.opentelemetry.kotlin.factory.TraceFlagsFactory
import io.opentelemetry.kotlin.factory.TraceStateFactory

/**
 * W3C Trace Context HTTP header propagator.
 *
 * https://www.w3.org/TR/trace-context/
 */
@OptIn(ExperimentalApi::class)
internal class W3CTraceContextPropagator(
    private val traceFlagsFactory: TraceFlagsFactory,
    private val traceStateFactory: TraceStateFactory,
    private val spanContextFactory: SpanContextFactory,
    private val spanFactory: SpanFactory,
) : TextMapPropagator {

    override fun fields(): Collection<String> = FIELDS

    override fun <T> inject(context: Context, carrier: T, setter: TextMapSetter<T>) {
        val spanContext = context.extractSpan().spanContext
        if (!spanContext.isValid) {
            return
        }
        val traceparent = TraceParent(
            version = TraceParent.VERSION_00,
            traceId = spanContext.traceId,
            spanId = spanContext.spanId,
            traceFlags = spanContext.traceFlags,
        ).encode()
        setter.set(carrier, TRACEPARENT, traceparent)

        val tracestate = TraceStateMarshaller(spanContext.traceState).encode()
        if (tracestate.isNotEmpty()) {
            setter.set(carrier, TRACESTATE, tracestate)
        }
    }

    override fun <T> extract(context: Context, carrier: T, getter: TextMapGetter<T>): Context {
        val rawTraceparent = getter.get(carrier, TRACEPARENT) ?: return context
        val parsed = TraceParent.decode(rawTraceparent, traceFlagsFactory) ?: return context

        val rawTracestate = getter.get(carrier, TRACESTATE)
        val traceState = when (rawTracestate) {
            null -> traceStateFactory.default
            else -> TraceStateMarshaller.decode(rawTracestate, traceStateFactory).traceState
        }

        val spanContext = spanContextFactory.create(
            traceId = parsed.traceId,
            spanId = parsed.spanId,
            traceFlags = parsed.traceFlags,
            traceState = traceState,
            isRemote = true,
        )
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
