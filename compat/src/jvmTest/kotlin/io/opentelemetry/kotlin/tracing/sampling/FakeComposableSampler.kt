package io.opentelemetry.kotlin.tracing.sampling

import io.opentelemetry.kotlin.attributes.AttributeContainer
import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.tracing.SpanKind
import io.opentelemetry.kotlin.tracing.TraceState
import io.opentelemetry.kotlin.tracing.model.SpanLink

internal class FakeComposableSampler(
    var intent: SamplingIntent = FakeSamplingIntent(),
    override val description: String = "FakeComposableSampler",
) : ComposableSampler {

    var lastContext: Context? = null
        private set
    var lastName: String? = null
        private set
    var lastSpanKind: SpanKind? = null
        private set
    var lastAttributes: AttributeContainer? = null
        private set
    var lastLinks: List<SpanLink>? = null
        private set

    override fun getSamplingIntent(
        context: Context,
        name: String,
        spanKind: SpanKind,
        attributes: AttributeContainer,
        links: List<SpanLink>,
    ): SamplingIntent {
        lastContext = context
        lastName = name
        lastSpanKind = spanKind
        lastAttributes = attributes
        lastLinks = links
        return intent
    }
}

internal class FakeSamplingIntent(
    override val threshold: Long? = 0L,
    override val adjustedCountReliable: Boolean = true,
    override val attributesProvider: (() -> AttributeContainer)? = null,
    override val traceStateProvider: ((TraceState, SamplingResult.Decision) -> TraceState)? = null,
) : SamplingIntent
