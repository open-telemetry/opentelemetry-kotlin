package io.opentelemetry.kotlin.tracing.ext

import io.opentelemetry.kotlin.aliases.OtelJavaTraceState
import io.opentelemetry.kotlin.tracing.TraceState
import io.opentelemetry.kotlin.tracing.createInvalidSpanContext

internal fun TraceState.toOtelJavaTraceState(): OtelJavaTraceState {
    return OtelJavaTraceState.builder().apply {
        try {
            asMap().entries.reversed().forEach {
                put(it.key, it.value)
            }
        } catch (exc: Throwable) {
            // Preserve existing behavior for custom TraceState implementations.
        }
    }.build()
}

internal fun Map<String, String>.toOtelJavaTraceState(): OtelJavaTraceState =
    OtelJavaTraceState.builder().apply {
        entries.toList().asReversed().forEach { (key, value) ->
            put(key, value)
        }
    }.build()

/**
 * Copies an opentelemetry-java trace state into the Kotlin [TraceState] implementation, preserving
 * entry order. Any entry that the Kotlin implementation rejects is dropped.
 */
internal fun OtelJavaTraceState.toOtelKotlinTraceState(): TraceState {
    val entries = mutableListOf<Pair<String, String>>()
    forEach { key, value -> entries.add(key to value) }

    // Reverse to preserve original order.
    return entries.asReversed().fold(createInvalidSpanContext().traceState) { state, (key, value) ->
        state.put(key, value)
    }
}
