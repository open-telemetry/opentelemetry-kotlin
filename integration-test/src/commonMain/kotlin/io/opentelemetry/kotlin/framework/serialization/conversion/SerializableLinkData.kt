package io.opentelemetry.kotlin.framework.serialization.conversion

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.framework.serialization.SerializableLinkData
import io.opentelemetry.kotlin.tracing.data.LinkData

@OptIn(ExperimentalApi::class)
fun LinkData.toSerializable() =
    SerializableLinkData(
        spanContext = spanContext.toSerializable(),
        attributes = attributes.toSerializable(),
        totalAttributeCount = attributes.size,
    )
