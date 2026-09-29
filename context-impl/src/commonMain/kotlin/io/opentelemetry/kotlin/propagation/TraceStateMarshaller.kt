package io.opentelemetry.kotlin.propagation

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.factory.buildTraceState
import io.opentelemetry.kotlin.tracing.TraceState

/**
 * Implementation of a W3C `tracestate` header.
 *
 * https://www.w3.org/TR/trace-context-2/#tracestate-header
 */
@OptIn(ExperimentalApi::class)
public class TraceStateMarshaller(public val traceState: TraceState) {

    private val state by lazy {
        traceState.asMap()
    }

    fun encode(): String = W3CTraceStateCodec.encode(state)

    companion object {
        fun decode(header: String): TraceStateMarshaller {
            val decodedMap = W3CTraceStateCodec.decode(header)
            // preserves header order and drops invalid entries
            val traceState = buildTraceState {
                decodedMap.forEach { (key, value) -> put(key, value) }
            }
            return TraceStateMarshaller(traceState)
        }
    }
}
