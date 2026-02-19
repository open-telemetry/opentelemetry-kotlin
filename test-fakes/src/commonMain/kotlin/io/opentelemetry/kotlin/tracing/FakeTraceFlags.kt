package io.opentelemetry.kotlin.tracing

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.tracing.model.TraceFlags

@OptIn(ExperimentalApi::class)
class FakeTraceFlags(
    override val isSampled: Boolean = false,
    override val isRandom: Boolean = false,
) : TraceFlags
