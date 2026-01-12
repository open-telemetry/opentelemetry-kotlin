package io.opentelemetry.kotlin.framework.serialization.conversion

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.framework.serialization.SerializableSpanStatusData
import io.opentelemetry.kotlin.tracing.data.StatusData

@OptIn(ExperimentalApi::class)
fun StatusData.toSerializable() =
    SerializableSpanStatusData(
        name = statusCode.name,
        description = description.orEmpty(),
    )
