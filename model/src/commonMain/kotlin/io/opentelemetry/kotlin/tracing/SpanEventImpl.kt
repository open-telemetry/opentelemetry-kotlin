package io.opentelemetry.kotlin.tracing

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.attributes.MutableAttributeContainer
import io.opentelemetry.kotlin.tracing.model.SpanEvent

@OptIn(ExperimentalApi::class)
class SpanEventImpl(
    override val name: String,
    override val timestamp: Long,
    private val attributesContainer: MutableAttributeContainer
) : SpanEvent, MutableAttributeContainer by attributesContainer
