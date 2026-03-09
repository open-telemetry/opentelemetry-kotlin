package io.opentelemetry.kotlin.tracing

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.attributes.AttributeContainer
import io.opentelemetry.kotlin.attributes.AttributesMutator
import io.opentelemetry.kotlin.attributes.CompatAttributesModel
import io.opentelemetry.kotlin.tracing.model.SpanContext
import io.opentelemetry.kotlin.tracing.model.SpanLink

@OptIn(ExperimentalApi::class)
internal class SpanLinkCompatImpl(
    override val spanContext: SpanContext,
    private val attrs: CompatAttributesModel
) : SpanLink, AttributesMutator by attrs, AttributeContainer by attrs
