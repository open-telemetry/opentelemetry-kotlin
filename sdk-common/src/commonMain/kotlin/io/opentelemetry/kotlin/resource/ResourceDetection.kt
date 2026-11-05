package io.opentelemetry.kotlin.resource

import io.opentelemetry.kotlin.error.SdkError
import io.opentelemetry.kotlin.error.SdkErrorHandler
import io.opentelemetry.kotlin.error.SdkErrorSeverity
import io.opentelemetry.kotlin.error.guardOrDefault
import io.opentelemetry.kotlin.error.reportError
import io.opentelemetry.kotlin.factory.ResourceFactory

/**
 * Runs each [ResourceDetector] in order and merges the results into a single [Resource].
 * Later detectors take precedence over earlier ones where they set the same attribute key.
 *
 * A detector that throws is reported to [errorHandler] and contributes nothing.
 *
 * https://opentelemetry.io/docs/specs/otel/resource/sdk/#detecting-resource-information-from-the-environment
 */
public fun List<ResourceDetector>.detectResource(
    factory: ResourceFactory,
    errorHandler: SdkErrorHandler,
): Resource {
    val namedDetectors = mapNotNull { detector ->
        errorHandler.guardOrDefault(null, "Failed to read resource detector name, skipping detector") {
            detector.name to detector
        }
    }
    namedDetectors.reportDuplicateNames(errorHandler)

    return namedDetectors.fold(factory.empty) { detected, (name, detector) ->
        try {
            val detectedResource = with(detector) { factory.detect() }
            val merged = detected.merge(detectedResource)
            reportSchemaUrlConflict(detected, detectedResource, detector.name, errorHandler)
            merged
        } catch (exc: Throwable) {
            errorHandler.reportError(
                SdkError.UserCodeError(
                    exc,
                    "Resource detector '$name' failed",
                    SdkErrorSeverity.ERROR,
                )
            )
            detected
        }
    }
}

/**
 * The OTel specification requires that detector names are unique, so that they can be referenced
 * unambiguously in configuration. Duplicates are reported but not rejected - every detector still
 * runs.
 */
private fun List<Pair<String, ResourceDetector>>.reportDuplicateNames(errorHandler: SdkErrorHandler) {
    groupBy { (name, _) -> name }
        .filterValues { it.size > 1 }
        .keys
        .forEach { name ->
            errorHandler.reportError(
                SdkError.ApiMisuse(
                    "ResourceDetector",
                    "Multiple resource detectors are named '$name'",
                    SdkErrorSeverity.WARNING,
                )
            )
        }
}

/**
 * Reports an error when combining detectors with different non-empty schema URLs.
 * The spec requires this to be an error condition.
 *
 * https://opentelemetry.io/docs/specs/otel/resource/sdk/#resource-detector
 */
private fun reportSchemaUrlConflict(
    detected: Resource,
    newResource: Resource,
    detectorName: String,
    errorHandler: SdkErrorHandler,
) {
    val detectedSchema = detected.schemaUrl
    val newSchema = newResource.schemaUrl

    // Treat empty strings as absent (no schema URL)
    val detectedSchemaEffective = detectedSchema?.takeIf { it.isNotEmpty() }
    val newSchemaEffective = newSchema?.takeIf { it.isNotEmpty() }

    // Only report conflict if both are non-empty and different
    if (detectedSchemaEffective != null && newSchemaEffective != null && detectedSchemaEffective != newSchemaEffective) {
        errorHandler.reportError(
            SdkError.ApiMisuse(
                "ResourceDetector",
                "Resource detector '$detectorName' has conflicting schema URL: " +
                    "'$newSchemaEffective' does not match detected resource schema URL '$detectedSchemaEffective'",
                SdkErrorSeverity.ERROR,
            )
        )
    }
}
