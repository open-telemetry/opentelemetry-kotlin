package io.opentelemetry.kotlin.tracing

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.propagation.W3CTraceStateValidator

@ExperimentalApi
internal class TraceStateImpl private constructor(
    private val data: Map<String, String>
) : TraceState {

    companion object {
        fun create(): TraceState = TraceStateImpl(emptyMap())
    }

    override fun get(key: String): String? = data[key]

    override fun asMap(): Map<String, String> = data.toMap()

    override fun put(key: String, value: String): TraceState {
        if (!W3CTraceStateValidator.canPut(data, key, value)) {
            return this
        }
        return TraceStateImpl(data + (key to value))
    }

    override fun remove(key: String): TraceState {
        if (!data.containsKey(key)) {
            return this
        }

        return TraceStateImpl(data - key)
    }
}
