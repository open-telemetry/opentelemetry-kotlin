package io.opentelemetry.kotlin.tracing.sampling

import io.opentelemetry.kotlin.attributes.AttributeContainer
import io.opentelemetry.kotlin.tracing.model.TraceState
import io.opentelemetry.kotlin.tracing.sampling.SamplingResult.Decision

internal class SamplingResultImpl(
    override val decision: Decision,
    override val attributes: AttributeContainer,
    override val traceState: TraceState,
) : SamplingResult
