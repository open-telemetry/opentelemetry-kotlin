package io.opentelemetry.kotlin.tracing

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.attributes.AttributeContainer
import io.opentelemetry.kotlin.attributes.AttributesModel
import io.opentelemetry.kotlin.attributes.MutableAttributeContainer
import io.opentelemetry.kotlin.tracing.model.SpanEvent

@OptIn(ExperimentalApi::class)
internal class SpanEventImpl(
    override val name: String,
    override val timestamp: Long,
    private val attrs: AttributesModel
) : SpanEvent, MutableAttributeContainer by attrs, AttributeContainer by attrs
