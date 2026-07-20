package io.opentelemetry.kotlin.tracing

class FakeTraceFlags(
    override val isSampled: Boolean = true,
    override val isRandom: Boolean = false,
) : TraceFlags
