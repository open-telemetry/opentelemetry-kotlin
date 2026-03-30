package io.opentelemetry.kotlin.factory

import io.opentelemetry.kotlin.tracing.FakeTraceState
import io.opentelemetry.kotlin.tracing.TraceState

class FakeTraceStateFactory : TraceStateFactory {
    override val default: TraceState = FakeTraceState()
}
