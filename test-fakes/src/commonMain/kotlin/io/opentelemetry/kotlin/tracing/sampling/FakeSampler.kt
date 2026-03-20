package io.opentelemetry.kotlin.tracing.sampling

import io.opentelemetry.kotlin.attributes.AttributeContainer
import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.tracing.FakeTraceState
import io.opentelemetry.kotlin.tracing.SpanKind
import io.opentelemetry.kotlin.tracing.TraceState
import io.opentelemetry.kotlin.tracing.model.SpanLink

class FakeSampler(
    private val decision: SamplingResult.Decision = SamplingResult.Decision.RECORD_AND_SAMPLE,
) : Sampler {

    var callCount = 0
        private set

    override fun shouldSample(
        context: Context,
        traceId: String,
        name: String,
        spanKind: SpanKind,
        attributes: AttributeContainer,
        links: List<SpanLink>,
    ): SamplingResult {
        callCount++
        return FakeSamplingResult(decision)
    }

    override val description: String = "FakeSampler"

    private class FakeSamplingResult(
        override val decision: SamplingResult.Decision,
        override val attributes: AttributeContainer = FakeEmptyAttributeContainer,
        override val traceState: TraceState = FakeTraceState(emptyMap()),
    ) : SamplingResult

    private object FakeEmptyAttributeContainer : AttributeContainer {
        override val attributes: Map<String, Any> = emptyMap()
    }
}
