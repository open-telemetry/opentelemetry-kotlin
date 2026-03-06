package io.opentelemetry.kotlin.tracing.ext

import io.opentelemetry.kotlin.aliases.OtelJavaLinkData
import io.opentelemetry.kotlin.attributes.attrsFromMap
import io.opentelemetry.kotlin.tracing.data.SpanLinkData

public fun SpanLinkData.toOtelJavaLinkData(): OtelJavaLinkData = OtelJavaLinkData.create(
    spanContext.toOtelJavaSpanContext(),
    attrsFromMap(attributes)
)
