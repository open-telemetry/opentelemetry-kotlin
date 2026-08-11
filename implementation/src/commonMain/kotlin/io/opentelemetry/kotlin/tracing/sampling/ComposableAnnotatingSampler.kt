package io.opentelemetry.kotlin.tracing.sampling

import io.opentelemetry.kotlin.attributes.AttributeContainer
import io.opentelemetry.kotlin.attributes.AttributesModel
import io.opentelemetry.kotlin.attributes.AttributesMutator
import io.opentelemetry.kotlin.attributes.setAttributes
import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.tracing.SpanKind
import io.opentelemetry.kotlin.tracing.model.SpanLink

/**
 * A [ComposableSampler] that delegates the sampling decision to [delegate], adding [annotations]
 * to the span if it ends up being sampled.
 *
 * https://opentelemetry.io/docs/specs/otel/trace/sdk/#composableannotating
 */
internal class ComposableAnnotatingSampler(
    private val delegate: ComposableSampler,
    attributes: AttributesMutator.() -> Unit,
) : ComposableSampler {

    private val annotations: AttributesModel = AttributesModel().apply(attributes)

    override fun getSamplingIntent(
        context: Context,
        name: String,
        spanKind: SpanKind,
        attributes: AttributeContainer,
        links: List<SpanLink>
    ): SamplingIntent {
        val intent = delegate.getSamplingIntent(context, name, spanKind, attributes, links)
        val delegateAttributes = intent.attributesProvider

        return SamplingIntentImpl(
            threshold = intent.threshold,
            adjustedCountReliable = intent.adjustedCountReliable,
            attributesProvider = if (delegateAttributes == null) {
                { annotations }
            } else {
                { merge(delegateAttributes()) }
            },
            traceStateProvider = intent.traceStateProvider,
        )
    }

    /**
     * Combines the delegate's attributes with [annotations], which are applied last so that they
     * win on key collision.
     */
    private fun merge(delegateAttributes: AttributeContainer): AttributeContainer =
        AttributesModel().apply {
            setAttributes(delegateAttributes.attributes)
            setAttributes(annotations.attributes)
        }

    override val description: String
        get() = "ComposableAnnotatingSampler{delegate:${delegate.description}}"
}
