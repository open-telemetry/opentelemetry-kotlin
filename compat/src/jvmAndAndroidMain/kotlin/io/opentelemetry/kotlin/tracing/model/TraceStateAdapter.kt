package io.opentelemetry.kotlin.tracing.model

import io.opentelemetry.kotlin.aliases.OtelJavaTraceState

internal class TraceStateAdapter(
    private val traceState: OtelJavaTraceState
) : TraceState {

    override fun get(key: String): String? = traceState.get(key)
    override fun asMap(): Map<String, String> = traceState.asMap()

    override fun put(key: String, value: String): TraceState {
        val newTraceState = traceState.toBuilder().put(key, value).build()
        return TraceStateAdapter(newTraceState)
    }

    override fun remove(key: String): TraceState {
        val newTraceState = traceState.toBuilder().remove(key).build()
        return TraceStateAdapter(newTraceState)
    }
}
