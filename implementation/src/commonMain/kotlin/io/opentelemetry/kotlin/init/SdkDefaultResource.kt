package io.opentelemetry.kotlin.init

import io.opentelemetry.kotlin.BuildKonfig
import io.opentelemetry.kotlin.attributes.AttributesModel
import io.opentelemetry.kotlin.attributes.NO_ATTRIBUTE_LIMIT
import io.opentelemetry.kotlin.resource.Resource
import io.opentelemetry.kotlin.resource.ResourceImpl
import io.opentelemetry.kotlin.semconv.SemconvBuildKonfig
import io.opentelemetry.kotlin.semconv.ServiceAttributes
import io.opentelemetry.kotlin.semconv.TelemetryAttributes

internal fun sdkDefaultResource(): Resource = ResourceImpl(
    container = AttributesModel(
        attributeLimit = NO_ATTRIBUTE_LIMIT,
        attrs = mutableMapOf(
            ServiceAttributes.SERVICE_NAME to "unknown_service",
            TelemetryAttributes.TELEMETRY_SDK_NAME to "opentelemetry",
            TelemetryAttributes.TELEMETRY_SDK_LANGUAGE to "kotlin",
            TelemetryAttributes.TELEMETRY_SDK_VERSION to BuildKonfig.SDK_VERSION,
        ),
    ),
    schemaUrl = SemconvBuildKonfig.SCHEMA_URL,
)
