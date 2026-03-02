package io.opentelemetry.kotlin.tracing

import io.opentelemetry.kotlin.attributes.MutableAttributeContainer
import io.opentelemetry.kotlin.tracing.model.Link
import io.opentelemetry.kotlin.tracing.model.SpanContext

class LinkImpl(
    override val spanContext: SpanContext,
    private val attributesContainer: MutableAttributeContainer
) : Link, MutableAttributeContainer by attributesContainer
