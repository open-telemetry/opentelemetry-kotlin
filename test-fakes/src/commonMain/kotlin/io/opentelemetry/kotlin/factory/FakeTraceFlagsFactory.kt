package io.opentelemetry.kotlin.factory

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.tracing.FakeTraceFlags
import io.opentelemetry.kotlin.tracing.model.TraceFlags

@OptIn(ExperimentalApi::class)
internal class FakeTraceFlagsFactory : TraceFlagsFactory {
    override val default: TraceFlags = FakeTraceFlags()
    override fun create(
        sampled: Boolean,
        random: Boolean
    ): TraceFlags = FakeTraceFlags()

    override fun fromHex(hex: String): TraceFlags = FakeTraceFlags()
}
