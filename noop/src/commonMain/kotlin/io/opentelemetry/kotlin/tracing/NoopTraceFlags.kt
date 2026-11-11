package io.opentelemetry.kotlin.tracing

import io.opentelemetry.kotlin.ExperimentalApi

@OptIn(ExperimentalApi::class)
internal object NoopTraceFlags : TraceFlags {
    override val isSampled: Boolean = false
    override val isRandom: Boolean = false
}
