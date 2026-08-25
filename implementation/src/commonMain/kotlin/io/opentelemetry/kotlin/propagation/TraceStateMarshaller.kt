package io.opentelemetry.kotlin.propagation

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.factory.TraceStateFactory
import io.opentelemetry.kotlin.tracing.TraceState

internal const val MAX_TRACESTATE_CHARS = 512
internal const val LARGE_MEMBER_CHARS = 128

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

    fun encode(): String = W3CTraceStateCodec.encode(state.truncatedToHeaderLimit())

    companion object {
        fun decode(header: String, traceStateFactory: TraceStateFactory): TraceStateMarshaller {
            var state = traceStateFactory.default
            W3CTraceStateCodec.decode(header).forEach { (key, value) ->
                val next = state.put(key, value)
                if (next.get(key) == value) {
                    state = next
                }
            }
            val truncated = state.asMap().truncatedToHeaderLimit()
            state = truncated.entries.fold(traceStateFactory.default) { acc, (key, value) ->
                acc.put(key, value)
            }
            return TraceStateMarshaller(state)
        }
    }
}

/**
 * Drops whole list-members until the encoded header is at most [MAX_TRACESTATE_CHARS].
 * Members longer than [LARGE_MEMBER_CHARS] are removed first, then members from the end.
 *
 * https://www.w3.org/TR/trace-context-2/#tracestate-limits
 */
private fun Map<String, String>.truncatedToHeaderLimit(): Map<String, String> {
    if (W3CTraceStateCodec.encode(this).length <= MAX_TRACESTATE_CHARS) {
        return this
    }
    val result = LinkedHashMap(this)
    while (result.isNotEmpty() &&
        W3CTraceStateCodec.encode(result).length > MAX_TRACESTATE_CHARS
    ) {
        val largeKey = result.entries.findLast { (key, value) ->
            key.length + 1 + value.length > LARGE_MEMBER_CHARS
        }?.key
        if (largeKey != null) {
            result.remove(largeKey)
        } else {
            result.remove(result.keys.last())
        }
    }
    return result
}
