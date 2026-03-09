package io.opentelemetry.kotlin.tracing

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.attributes.AttributeContainer
import io.opentelemetry.kotlin.attributes.AttributesMutator
import io.opentelemetry.kotlin.attributes.CompatAttributesModel
import io.opentelemetry.kotlin.tracing.model.SpanEvent

@OptIn(ExperimentalApi::class)
internal class SpanEventCompatImpl(
    override val name: String,
    override val timestamp: Long,
    private val attrs: CompatAttributesModel
) : SpanEvent, AttributesMutator by attrs, AttributeContainer by attrs
