package io.opentelemetry.kotlin.tracing.ext

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaLinkData
import io.opentelemetry.kotlin.attributes.attrsFromMap
import io.opentelemetry.kotlin.tracing.data.SpanLinkData

/**
 * Copies an opentelemetry-kotlin [SpanLinkData] into opentelemetry-java link data. The Java total
 * attribute count includes [SpanLinkData.droppedAttributesCount].
 */
@ExperimentalApi
public fun SpanLinkData.toOtelJavaLinkData(): OtelJavaLinkData = OtelJavaLinkData.create(
    spanContext.toOtelJavaSpanContext(),
    attrsFromMap(attributes),
    attributes.size + droppedAttributesCount
)
