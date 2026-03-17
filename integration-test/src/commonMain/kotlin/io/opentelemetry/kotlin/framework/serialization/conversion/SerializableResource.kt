package io.opentelemetry.kotlin.framework.serialization.conversion

import io.opentelemetry.kotlin.framework.serialization.SerializableResource
import io.opentelemetry.kotlin.resource.Resource
import io.opentelemetry.kotlin.semconv.ServiceAttributes
import io.opentelemetry.kotlin.semconv.TelemetryAttributes

fun Resource.toSerializable() =
    SerializableResource(
        schemaUrl = schemaUrl.toString(),
        attributes = attributes.mapValues {
            when (it.key) {
                ServiceAttributes.SERVICE_VERSION -> "UNKNOWN"
                TelemetryAttributes.TELEMETRY_SDK_VERSION -> "UNKNOWN"
                else -> it.value
            }
        }.toSerializable(),
    )
