package io.opentelemetry.kotlin

import io.opentelemetry.kotlin.semconv.TelemetryAttributes
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal val sdkDefaultAttributes: Map<String, Any> = mapOf(
    TelemetryAttributes.TELEMETRY_SDK_NAME to "opentelemetry",
    TelemetryAttributes.TELEMETRY_SDK_LANGUAGE to "kotlin",
    TelemetryAttributes.TELEMETRY_SDK_VERSION to BuildKonfig.SDK_VERSION,
)

internal fun assertHasSdkDefaultAttributes(attributes: Map<String, Any>) {
    assertEquals("opentelemetry", attributes[TelemetryAttributes.TELEMETRY_SDK_NAME])
    assertEquals("kotlin", attributes[TelemetryAttributes.TELEMETRY_SDK_LANGUAGE])
    assertTrue(attributes.containsKey(TelemetryAttributes.TELEMETRY_SDK_VERSION))
}
