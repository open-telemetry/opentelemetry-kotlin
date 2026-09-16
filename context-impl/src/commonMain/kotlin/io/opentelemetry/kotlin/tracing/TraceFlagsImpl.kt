package io.opentelemetry.kotlin.tracing

public class TraceFlagsImpl(
    override val isSampled: Boolean,
    override val isRandom: Boolean
) : TraceFlags
