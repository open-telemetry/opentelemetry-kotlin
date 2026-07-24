package io.opentelemetry.kotlin.tracing.sampling

import io.opentelemetry.kotlin.attributes.AttributeContainer
import io.opentelemetry.kotlin.tracing.TraceState

internal data class SamplingIntentImpl(
    override val threshold: Long?,
    override val adjustedCountReliable: Boolean,
    override val attributesProvider: (() -> AttributeContainer)? = null,
    override val traceStateProvider: ((TraceState, SamplingResult.Decision) -> TraceState)? = null
) : SamplingIntent
