package io.opentelemetry.kotlin.tracing.sampling

import io.opentelemetry.kotlin.attributes.AttributeContainer
import io.opentelemetry.kotlin.tracing.model.TraceState

internal class SamplingResultImpl(
    override val decision: SamplingResult.Decision,
    override val attributes: AttributeContainer,
    override val traceState: TraceState,
) : SamplingResult
