package io.opentelemetry.kotlin.tracing

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.propagation.utils.W3CTraceStateValidator

@ExperimentalApi
public class TraceStateImpl internal constructor(
    private val data: LinkedHashMap<String, String>
) : TraceState {

    public companion object {
        public val EMPTY: TraceState = TraceStateImpl(linkedMapOf())
    }

    override fun get(key: String): String? = data[key]

    override fun asMap(): Map<String, String> = data.toMap()

    override fun put(key: String, value: String): TraceState {
        if (!W3CTraceStateValidator.canPut(data, key, value)) {
            return this
        }
        // Per W3C spec: modified keys MUST be moved to the beginning (left) of the list
        // New keys SHOULD be added to the beginning of the list
        val newData = linkedMapOf<String, String>()
        newData[key] = value
        data.forEach { (k, v) ->
            if (k != key) {
                newData[k] = v
            }
        }
        return TraceStateImpl(newData)
    }

    override fun remove(key: String): TraceState {
        if (!data.containsKey(key)) {
            return this
        }

        // Preserve order when removing key
        val newData = linkedMapOf<String, String>()
        data.forEach { (k, v) ->
            if (k != key) {
                newData[k] = v
            }
        }
        return TraceStateImpl(newData)
    }

    // W3C tracestate order is significant, so compare ordered entries rather than map equality
    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other !is TraceStateImpl) {
            return false
        }
        return data.entries.toList() == other.data.entries.toList()
    }

    override fun hashCode(): Int = data.entries.toList().hashCode()

    override fun toString(): String =
        data.entries.joinToString(separator = ",", prefix = "TraceStateImpl(", postfix = ")") {
            "${it.key}=${it.value}"
        }
}
