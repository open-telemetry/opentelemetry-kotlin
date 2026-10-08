package io.opentelemetry.kotlin

import io.opentelemetry.kotlin.resource.SDK_DEFAULT_SERVICE_NAME
import io.opentelemetry.kotlin.resource.SdkMode
import io.opentelemetry.kotlin.resource.TELEMETRY_SDK_MODE
import io.opentelemetry.kotlin.semconv.SemconvBuildKonfig
import io.opentelemetry.kotlin.semconv.ServiceAttributes
import io.opentelemetry.kotlin.semconv.TelemetryAttributes
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal val sdkDefaultSchemaUrl: String = SemconvBuildKonfig.SCHEMA_URL

internal val sdkDefaultAttributes: Map<String, Any> = mapOf(
    ServiceAttributes.SERVICE_NAME to SDK_DEFAULT_SERVICE_NAME,
    TelemetryAttributes.TELEMETRY_SDK_NAME to "opentelemetry",
    TelemetryAttributes.TELEMETRY_SDK_LANGUAGE to "kotlin",
    TelemetryAttributes.TELEMETRY_SDK_VERSION to SdkBuildKonfig.SDK_VERSION,
    TELEMETRY_SDK_MODE to SdkMode.REGULAR.attributeValue,
)

internal fun assertHasSdkDefaultAttributes(attributes: Map<String, Any>) {
    assertEquals(SDK_DEFAULT_SERVICE_NAME, attributes[ServiceAttributes.SERVICE_NAME])
    assertEquals("opentelemetry", attributes[TelemetryAttributes.TELEMETRY_SDK_NAME])
    assertEquals("kotlin", attributes[TelemetryAttributes.TELEMETRY_SDK_LANGUAGE])
    assertTrue(attributes.containsKey(TelemetryAttributes.TELEMETRY_SDK_VERSION))
    assertEquals(SdkMode.REGULAR.attributeValue, attributes[TELEMETRY_SDK_MODE])
}
