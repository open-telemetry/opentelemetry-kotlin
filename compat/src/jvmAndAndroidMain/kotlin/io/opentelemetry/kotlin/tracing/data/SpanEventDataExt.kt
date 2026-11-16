package io.opentelemetry.kotlin.tracing.data

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaEventData
import io.opentelemetry.kotlin.attributes.attrsFromMap

/**
 * Copies an opentelemetry-kotlin [SpanEventData] into opentelemetry-java event data. The Java total
 * attribute count includes [SpanEventData.droppedAttributesCount].
 */
@ExperimentalApi
public fun SpanEventData.toOtelJavaEventData(): OtelJavaEventData = OtelJavaEventData.create(
    timestamp,
    name,
    attrsFromMap(attributes),
    attributes.size + droppedAttributesCount
)
