package io.opentelemetry.kotlin.resource

import io.opentelemetry.kotlin.error.SdkError
import io.opentelemetry.kotlin.error.SdkErrorHandler
import io.opentelemetry.kotlin.error.SdkErrorSeverity
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
    reportDuplicateNames(errorHandler)

    return fold(factory.empty) { detected, detector ->
        try {
            detected.merge(with(detector) { factory.detect() })
        } catch (exc: Throwable) {
            errorHandler.reportError(
                SdkError.UserCodeError(
                    exc,
                    "Resource detector '${detector.name}' failed",
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
private fun List<ResourceDetector>.reportDuplicateNames(errorHandler: SdkErrorHandler) {
    groupBy(ResourceDetector::name)
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
