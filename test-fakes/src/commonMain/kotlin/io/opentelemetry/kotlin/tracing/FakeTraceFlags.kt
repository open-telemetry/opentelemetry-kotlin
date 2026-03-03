package io.opentelemetry.kotlin.tracing

import io.opentelemetry.kotlin.tracing.model.TraceFlags

class FakeTraceFlags(
    override val isSampled: Boolean = false,
    override val isRandom: Boolean = false,
) : TraceFlags
