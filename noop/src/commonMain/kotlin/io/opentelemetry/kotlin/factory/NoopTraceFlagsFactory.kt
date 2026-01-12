package io.opentelemetry.kotlin.factory

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.tracing.NoopTraceFlags
import io.opentelemetry.kotlin.tracing.model.TraceFlags

@OptIn(ExperimentalApi::class)
internal object NoopTraceFlagsFactory : TraceFlagsFactory {
    override val default: TraceFlags = NoopTraceFlags
    override fun create(sampled: Boolean, random: Boolean): TraceFlags = NoopTraceFlags
    override fun fromHex(hex: String): TraceFlags = NoopTraceFlags
}
