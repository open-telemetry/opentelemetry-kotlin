package io.opentelemetry.kotlin.propagation

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.factory.TraceStateFactory
import io.opentelemetry.kotlin.tracing.TraceState
import io.opentelemetry.kotlin.tracing.TraceStateImpl
import io.opentelemetry.kotlin.propagation.W3CTraceStateValidator

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
        fun decode(header: String, traceStateFactory: TraceStateFactory): TraceStateMarshaller {
            val decodedMap = W3CTraceStateCodec.decode(header)
            // Build TraceState directly from the decoded map to preserve order
            // Apply validation to filter out invalid entries
            val filteredMap = linkedMapOf<String, String>()
            decodedMap.forEach { (key, value) ->
                if (W3CTraceStateValidator.canPut(filteredMap, key, value)) {
                    filteredMap[key] = value
                }
            }
            val state = TraceStateImpl.fromMap(filteredMap)
            return TraceStateMarshaller(state)
        }
    }
}
