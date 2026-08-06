package io.opentelemetry.kotlin.framework.serialization.conversion

import io.opentelemetry.kotlin.framework.serialization.SerializableResource
import io.opentelemetry.kotlin.resource.Resource
import io.opentelemetry.kotlin.semconv.SemconvBuildKonfig
import io.opentelemetry.kotlin.semconv.TelemetryAttributes

/**
 * Placeholder recorded in golden files in place of the SDK's default schema URL
 */
const val SEMCONV_SCHEMA_URL_PLACEHOLDER = "SEMCONV_SCHEMA_URL"

fun Resource.toSerializable() =
    SerializableResource(
        schemaUrl = when (schemaUrl) {
            SemconvBuildKonfig.SCHEMA_URL -> SEMCONV_SCHEMA_URL_PLACEHOLDER
            else -> schemaUrl.toString()
        },
        attributes = attributes.mapValues {
            when (it.key) {
                TelemetryAttributes.TELEMETRY_SDK_VERSION -> "UNKNOWN"
                else -> it.value
            }
        }.toSerializable(),
    )
