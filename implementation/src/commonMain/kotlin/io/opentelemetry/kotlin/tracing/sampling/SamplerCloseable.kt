package io.opentelemetry.kotlin.tracing.sampling

import io.opentelemetry.kotlin.export.OperationResultCode
import io.opentelemetry.kotlin.export.TelemetryCloseable

/**
 * Adapts a [Sampler] so that it shuts down alongside the span processors. Samplers have nothing to
 * flush.
 */
internal class SamplerCloseable(private val sampler: Sampler) : TelemetryCloseable {
    override suspend fun forceFlush(): OperationResultCode = OperationResultCode.Success
    override suspend fun shutdown(): OperationResultCode = sampler.shutdown()
}
