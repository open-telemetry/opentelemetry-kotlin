package io.opentelemetry.kotlin.framework.serialization.conversion

import io.opentelemetry.kotlin.framework.serialization.SerializableSpanStatusData
import io.opentelemetry.kotlin.tracing.data.StatusData

fun StatusData.toSerializable() =
    SerializableSpanStatusData(
        name = statusCode.name,
        description = description.orEmpty(),
    )
