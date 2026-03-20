package io.opentelemetry.kotlin.tracing

import io.opentelemetry.kotlin.ExperimentalApi

@ExperimentalApi
internal object NoopTraceFlags : TraceFlags {
    override val isSampled: Boolean = false
    override val isRandom: Boolean = false
}
