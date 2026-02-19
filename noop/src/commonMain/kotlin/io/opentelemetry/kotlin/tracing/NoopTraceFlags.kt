package io.opentelemetry.kotlin.tracing

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.tracing.model.TraceFlags

@ExperimentalApi
internal object NoopTraceFlags : TraceFlags {
    override val isSampled: Boolean = false
    override val isRandom: Boolean = false
}
