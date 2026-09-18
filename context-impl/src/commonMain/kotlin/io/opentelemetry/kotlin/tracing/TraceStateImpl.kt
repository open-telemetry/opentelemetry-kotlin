package io.opentelemetry.kotlin.tracing

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.propagation.W3CTraceStateValidator

@ExperimentalApi
public class TraceStateImpl private constructor(
    private val data: LinkedHashMap<String, String>
) : TraceState {

    companion object {
        fun create(): TraceState = TraceStateImpl(linkedMapOf())

        internal fun fromMap(map: Map<String, String>): TraceState {
            // For decoding: preserve the order from the input map
            val linkedMap = linkedMapOf<String, String>()
            map.forEach { (k, v) -> linkedMap[k] = v }
            return TraceStateImpl(linkedMap)
        }
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
}
