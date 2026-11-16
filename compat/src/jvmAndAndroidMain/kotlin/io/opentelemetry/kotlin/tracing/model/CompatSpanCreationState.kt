package io.opentelemetry.kotlin.tracing.model

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaSpanBuilder
import io.opentelemetry.kotlin.attributes.AttributeContainer
import io.opentelemetry.kotlin.attributes.AttributesMutator
import io.opentelemetry.kotlin.attributes.CompatAttributesModel
import io.opentelemetry.kotlin.attributes.compatAttributes
import io.opentelemetry.kotlin.tracing.SpanContext
import io.opentelemetry.kotlin.tracing.SpanCreationAction
import io.opentelemetry.kotlin.tracing.SpanLinkCompatImpl
import io.opentelemetry.kotlin.tracing.toOtelJavaSpanContext

/**
 * Holds the attributes and links supplied in the span creation lambda so that they can be
 * applied to an [OtelJavaSpanBuilder] before the span is started. This is required for sampling.
 */
@OptIn(ExperimentalApi::class)
internal class CompatSpanCreationState(
    private val attrs: CompatAttributesModel = CompatAttributesModel(),
) : SpanCreationAction, AttributesMutator by attrs, AttributeContainer by attrs {

    private val linksImpl = mutableListOf<SpanLinkCompatImpl>()

    override fun addLink(
        spanContext: SpanContext,
        attributes: (AttributesMutator.() -> Unit)?
    ) {
        linksImpl.add(SpanLinkCompatImpl(spanContext, compatAttributes(attributes)))
    }

    fun applyTo(builder: OtelJavaSpanBuilder) {
        builder.setAllAttributes(attrs.otelJavaAttributes())
        linksImpl.forEach {
            builder.addLink(it.spanContext.toOtelJavaSpanContext(), it.attrs.otelJavaAttributes())
        }
    }
}
