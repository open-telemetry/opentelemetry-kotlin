package io.opentelemetry.kotlin.factory

import io.opentelemetry.kotlin.tracing.NoopTraceFlags
import io.opentelemetry.kotlin.tracing.model.TraceFlags

internal object NoopTraceFlagsFactory : TraceFlagsFactory {
    override val default: TraceFlags = NoopTraceFlags
    override fun fromHex(hex: String): TraceFlags = NoopTraceFlags
}
