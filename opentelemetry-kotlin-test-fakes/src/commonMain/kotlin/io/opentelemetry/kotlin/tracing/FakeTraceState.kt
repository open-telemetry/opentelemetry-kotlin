package io.opentelemetry.kotlin.tracing

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.tracing.model.TraceState

@OptIn(ExperimentalApi::class)
class FakeTraceState(
    private val attrs: Map<String, String> = mapOf("foo" to "bar")
) : TraceState {
    override fun get(key: String): String? = attrs[key]

    override fun asMap(): Map<String, String> = attrs.toMap()

    override fun put(key: String, value: String): TraceState {
        val newAttrs = attrs.toMutableMap()
        newAttrs[key] = value
        return FakeTraceState(newAttrs)
    }

    override fun remove(key: String): TraceState {
        if (!attrs.containsKey(key)) {
            return this
        }
        val newAttrs = attrs.toMutableMap()
        newAttrs.remove(key)
        return FakeTraceState(newAttrs)
    }
}
