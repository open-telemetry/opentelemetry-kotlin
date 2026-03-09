package io.opentelemetry.kotlin.tracing

import io.opentelemetry.kotlin.attributes.AttributeContainer
import io.opentelemetry.kotlin.attributes.AttributesModel
import io.opentelemetry.kotlin.attributes.AttributesMutator
import io.opentelemetry.kotlin.tracing.model.SpanEvent

internal class SpanEventImpl(
    override val name: String,
    override val timestamp: Long,
    private val attrs: AttributesModel
) : SpanEvent, AttributesMutator by attrs, AttributeContainer by attrs
