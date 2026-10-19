package io.opentelemetry.kotlin.resource

import io.opentelemetry.kotlin.SdkBuildKonfig
import io.opentelemetry.kotlin.factory.ResourceFactory
import io.opentelemetry.kotlin.semconv.SemconvBuildKonfig
import io.opentelemetry.kotlin.semconv.ServiceAttributes
import io.opentelemetry.kotlin.semconv.TelemetryAttributes

/**
 * The fallback value for `service.name` when none is supplied. Follows the `unknown_service:<name>`
 * convention used by other OTel SDKs.
 */
public const val SDK_DEFAULT_SERVICE_NAME: String = "unknown_service:kotlin"

/**
 * Resource attribute key recording which [SdkMode] produced the telemetry.
 */
public const val TELEMETRY_SDK_MODE: String = "telemetry.sdk.mode"

/**
 * The mode the SDK is running in.
 */
public enum class SdkMode(public val attributeValue: String) {

    /**
     * Uses opentelemetry-java under the hood.
     */
    COMPAT("compat"),

    /**
     * Uses the Kotlin Multiplatform implementation.
     */
    REGULAR("regular"),
}

/**
 * The base [Resource] that every SDK mode (compat and regular) uses.
 *
 * https://opentelemetry.io/docs/specs/otel/resource/sdk/#sdk-provided-resource-attributes
 */
public fun ResourceFactory.sdkDefaultResource(mode: SdkMode): Resource = create(SemconvBuildKonfig.SCHEMA_URL) {
    setStringAttribute(ServiceAttributes.SERVICE_NAME, SDK_DEFAULT_SERVICE_NAME)
    setStringAttribute(TelemetryAttributes.TELEMETRY_SDK_NAME, "opentelemetry")
    setStringAttribute(TelemetryAttributes.TELEMETRY_SDK_LANGUAGE, "kotlin")
    setStringAttribute(TelemetryAttributes.TELEMETRY_SDK_VERSION, SdkBuildKonfig.SDK_VERSION)
    setStringAttribute(TELEMETRY_SDK_MODE, mode.attributeValue)
}
