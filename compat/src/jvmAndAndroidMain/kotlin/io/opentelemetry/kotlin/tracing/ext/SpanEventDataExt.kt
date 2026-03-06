package io.opentelemetry.kotlin.tracing.ext

import io.opentelemetry.kotlin.aliases.OtelJavaEventData
import io.opentelemetry.kotlin.attributes.attrsFromMap
import io.opentelemetry.kotlin.tracing.data.SpanEventData

public fun SpanEventData.toOtelJavaEventData(): OtelJavaEventData = OtelJavaEventData.create(
    timestamp,
    name,
    attrsFromMap(attributes)
)
