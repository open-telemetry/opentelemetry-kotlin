package io.opentelemetry.kotlin.framework.serialization.conversion

import io.opentelemetry.kotlin.framework.serialization.SerializableResource
import io.opentelemetry.kotlin.resource.Resource
import io.opentelemetry.kotlin.semconv.SemconvBuildKonfig
import io.opentelemetry.kotlin.semconv.ServiceAttributes
import io.opentelemetry.kotlin.semconv.TelemetryAttributes

/**
 * Placeholder recorded in golden files in place of the SDK's default schema URL
 */
const val SEMCONV_SCHEMA_URL_PLACEHOLDER = "SEMCONV_SCHEMA_URL"

/**
 * Placeholder recorded in golden files in place of the SDK's default `service.name`, which
 * includes the process executable name and so differs per platform
 */
const val SDK_DEFAULT_SERVICE_NAME_PLACEHOLDER = "SDK_DEFAULT_SERVICE_NAME"

fun Resource.toSerializable() =
    SerializableResource(
        schemaUrl = when (schemaUrl) {
            SemconvBuildKonfig.SCHEMA_URL -> SEMCONV_SCHEMA_URL_PLACEHOLDER
            else -> schemaUrl.toString()
        },
        attributes = attributes.mapValues {
            val value = it.value
            when {
                it.key == TelemetryAttributes.TELEMETRY_SDK_VERSION -> "UNKNOWN"
                it.key == ServiceAttributes.SERVICE_NAME && value is String && value.startsWith("unknown_service") ->
                    SDK_DEFAULT_SERVICE_NAME_PLACEHOLDER
                else -> value
            }
        }.toSerializable(),
    )
