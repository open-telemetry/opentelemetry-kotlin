package io.opentelemetry.kotlin.tracing.ext

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaLinkData
import io.opentelemetry.kotlin.attributes.attrsFromMap
import io.opentelemetry.kotlin.tracing.data.LinkData

@OptIn(ExperimentalApi::class)
public fun LinkData.toOtelJavaLinkData(): OtelJavaLinkData = OtelJavaLinkData.create(
    spanContext.toOtelJavaSpanContext(),
    attrsFromMap(attributes)
)
