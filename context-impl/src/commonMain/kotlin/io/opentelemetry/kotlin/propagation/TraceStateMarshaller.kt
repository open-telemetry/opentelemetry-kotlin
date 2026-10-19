package io.opentelemetry.kotlin.propagation

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.propagation.utils.W3CTraceStateCodec
import io.opentelemetry.kotlin.tracing.TraceState
import io.opentelemetry.kotlin.tracing.createSpanContext

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
            val traceState = createSpanContext(ByteArray(0), ByteArray(0)) {
                traceState {
                    decodedMap.forEach { (key, value) -> put(key, value) }
                }
            }.traceState
            return TraceStateMarshaller(traceState)
        }
    }
}
