package io.opentelemetry.kotlin.factory

import io.opentelemetry.kotlin.tracing.FakeTraceFlags
import io.opentelemetry.kotlin.tracing.TraceFlags

class FakeTraceFlagsFactory : TraceFlagsFactory {
    override val default: TraceFlags = FakeTraceFlags()
    override fun fromHex(hex: String): TraceFlags = FakeTraceFlags()
}
