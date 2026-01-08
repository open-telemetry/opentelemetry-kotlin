package io.opentelemetry.kotlin.tracing.ext

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaTraceState
import io.opentelemetry.kotlin.tracing.model.TraceState

@OptIn(ExperimentalApi::class)
internal fun TraceState.toOtelJavaTraceState(): OtelJavaTraceState {
    return OtelJavaTraceState.builder().apply {
        asMap().entries.forEach {
            put(it.key, it.value)
        }
    }.build()
}
