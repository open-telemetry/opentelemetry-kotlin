package io.opentelemetry.kotlin.config

import io.opentelemetry.kotlin.error.SdkErrorHandler
import io.opentelemetry.kotlin.error.SdkErrorSeverity

/**
 * Validate the given value against a validating function, and return that value if it's valid. Otherwise, return the provided default.
 */
public fun <T> validateOrUseDefault(
    sdkErrorHandler: SdkErrorHandler,
    api: String,
    configParameterName: String,
    value: T,
    default: T,
    validator: (T) -> Boolean,
): T = if (validator(value)) {
    value
} else {
    sdkErrorHandler.onApiMisuse(
        api,
        "$value is not a valid value for $configParameterName. $default used instead.",
        SdkErrorSeverity.WARNING,
    )
    default
}
