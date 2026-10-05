package io.opentelemetry.kotlin.tracing.ext

import io.opentelemetry.kotlin.aliases.OtelJavaTraceState
import io.opentelemetry.kotlin.factory.DefaultTraceStateFactory
import io.opentelemetry.kotlin.tracing.TraceState

internal fun TraceState.toOtelJavaTraceState(): OtelJavaTraceState {
    return OtelJavaTraceState.builder().apply {
        asMap().entries.reversed().forEach { // reverse to preserve original order
            put(it.key, it.value)
        }
    }.build()
}

/**
 * Copies an opentelemetry-java trace state into the Kotlin [TraceState] implementation, preserving
 * entry order. Any entry that the Kotlin implementation rejects is dropped.
 */
internal fun OtelJavaTraceState.toOtelKotlinTraceState(): TraceState {
    val entries = mutableListOf<Pair<String, String>>()
    forEach { key, value -> entries.add(key to value) }

    // reverse to preserve original order
    return entries.asReversed().fold(DefaultTraceStateFactory.default) { state, (key, value) ->
        state.put(key, value)
    }
}
