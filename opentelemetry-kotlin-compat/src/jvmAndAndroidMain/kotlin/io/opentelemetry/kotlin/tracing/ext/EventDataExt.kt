package io.opentelemetry.kotlin.tracing.ext

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaEventData
import io.opentelemetry.kotlin.attributes.attrsFromMap
import io.opentelemetry.kotlin.tracing.data.EventData

@OptIn(ExperimentalApi::class)
public fun EventData.toOtelJavaEventData(): OtelJavaEventData = OtelJavaEventData.create(
    timestamp,
    name,
    attrsFromMap(attributes)
)
