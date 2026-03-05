package io.opentelemetry.kotlin.factory

import io.opentelemetry.kotlin.tracing.FakeTraceFlags
import io.opentelemetry.kotlin.tracing.model.TraceFlags

internal class FakeTraceFlagsFactory : TraceFlagsFactory {
    override val default: TraceFlags = FakeTraceFlags()
    override fun fromHex(hex: String): TraceFlags = FakeTraceFlags()
}
