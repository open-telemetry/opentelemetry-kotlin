package io.opentelemetry.kotlin.tracing

class FakeTraceFlags(
    override val isSampled: Boolean = false,
    override val isRandom: Boolean = false,
) : TraceFlags
