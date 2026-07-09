package io.opentelemetry.kotlin.tracing.model

import io.opentelemetry.kotlin.attributes.AttributeContainer
import io.opentelemetry.kotlin.attributes.AttributesModel
import io.opentelemetry.kotlin.attributes.AttributesMutator
import io.opentelemetry.kotlin.init.config.SpanLimitConfig
import io.opentelemetry.kotlin.tracing.SpanContext
import io.opentelemetry.kotlin.tracing.SpanCreationAction

/**
 * Collects user-provided attributes and links from the [SpanCreationAction] lambda
 * before the sampling decision is made.
 *
 * Passing the collected state to [Sampler.shouldSample] allows samplers to use
 * user attributes and links as input for their decision, as required by the
 * OpenTelemetry specification. The span is not yet created at this point,
 * so [isRecording] checks are unnecessary.
 */
internal class SpanCreationCollector(
    private val spanLimitConfig: SpanLimitConfig,
    private val attrs: AttributesModel = AttributesModel(
        attributeLimit = spanLimitConfig.attributeCountLimit,
        attributeValueLengthLimit = spanLimitConfig.attributeValueLengthLimit
    )
) : SpanCreationAction, AttributesMutator by attrs {
    private val linksList = mutableListOf<SpanLink>()
    val attributes: AttributeContainer get() = attrs
    val links: List<SpanLink> get() = linksList.toList()

    override fun addLink(
        spanContext: SpanContext,
        attributes: (AttributesMutator.() -> Unit)?,
    ) {
        if (linksList.size < spanLimitConfig.linkCountLimit) {
            linksList.add(buildSpanLink(spanContext, attributes, spanLimitConfig))
        }
    }
}
