package io.opentelemetry.kotlin.factory

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaTraceState
import io.opentelemetry.kotlin.tracing.model.TraceState
import io.opentelemetry.kotlin.tracing.model.TraceStateAdapter

@OptIn(ExperimentalApi::class)
internal class CompatTraceStateFactory : TraceStateFactory {
    override val default: TraceState by lazy { TraceStateAdapter(OtelJavaTraceState.getDefault()) }
}
