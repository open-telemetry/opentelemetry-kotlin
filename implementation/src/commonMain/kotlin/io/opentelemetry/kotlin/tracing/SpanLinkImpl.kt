package io.opentelemetry.kotlin.tracing

import io.opentelemetry.kotlin.attributes.AttributeContainer
import io.opentelemetry.kotlin.attributes.AttributesModel
import io.opentelemetry.kotlin.attributes.MutableAttributeContainer
import io.opentelemetry.kotlin.tracing.model.Link
import io.opentelemetry.kotlin.tracing.model.SpanContext

internal class SpanLinkImpl(
    override val spanContext: SpanContext,
    private val attrs: AttributesModel
) : Link, MutableAttributeContainer by attrs, AttributeContainer by attrs
