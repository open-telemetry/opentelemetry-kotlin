package io.opentelemetry.kotlin.factory

import io.opentelemetry.kotlin.tracing.NoopTraceState
import io.opentelemetry.kotlin.tracing.TraceState

internal object NoopTraceStateFactory : TraceStateFactory {
    override val default: TraceState = NoopTraceState
}
