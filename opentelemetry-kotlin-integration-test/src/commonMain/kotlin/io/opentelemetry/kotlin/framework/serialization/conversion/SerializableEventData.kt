package io.opentelemetry.kotlin.framework.serialization.conversion

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.framework.serialization.SerializableEventData
import io.opentelemetry.kotlin.tracing.data.EventData

@OptIn(ExperimentalApi::class)
fun EventData.toSerializable() =
    SerializableEventData(
        name = name,
        attributes = attributes.toSerializable(),
        timestamp = timestamp,
        totalAttributesCount = attributes.size,
    )
