package io.opentelemetry.kotlin.propagation

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.factory.TraceStateFactory
import io.opentelemetry.kotlin.tracing.TraceState

/**
 * Implementation of a W3C `tracestate` header.
 *
 * https://www.w3.org/TR/trace-context-2/#tracestate-header
 */
@OptIn(ExperimentalApi::class)
internal class TraceStateMarshaller(internal val traceState: TraceState) {

    private val state by lazy {
        traceState.asMap()
    }

    fun encode(): String = W3CTraceStateCodec.encode(state)

    companion object {
        fun decode(header: String, traceStateFactory: TraceStateFactory): TraceStateMarshaller {
            var state = traceStateFactory.default
            W3CTraceStateCodec.decode(header).forEach { (key, value) ->
                val next = state.put(key, value)
                if (next.get(key) == value) {
                    state = next
                }
            }
            return TraceStateMarshaller(state)
        }
    }
}
