package io.opentelemetry.kotlin.tracing.sampling

import io.opentelemetry.kotlin.attributes.AttributeContainer
import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.tracing.FakeTraceState
import io.opentelemetry.kotlin.tracing.SpanKind
import io.opentelemetry.kotlin.tracing.TraceState
import io.opentelemetry.kotlin.tracing.model.SpanLink

class FakeSampler(
    private val decision: SamplingResult.Decision = SamplingResult.Decision.RECORD_AND_SAMPLE,
    private val samplerAttributes: Map<String, Any> = emptyMap(),
    private val samplerTraceState: TraceState = FakeTraceState(emptyMap()),
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
        return FakeSamplingResult(decision, FakeAttributeContainer(samplerAttributes), samplerTraceState)
    }

    override val description: String = "FakeSampler"

    private class FakeSamplingResult(
        override val decision: SamplingResult.Decision,
        override val attributes: AttributeContainer = FakeAttributeContainer(emptyMap()),
        override val traceState: TraceState = FakeTraceState(emptyMap()),
    ) : SamplingResult

    private class FakeAttributeContainer(
        override val attributes: Map<String, Any>
    ) : AttributeContainer
}
