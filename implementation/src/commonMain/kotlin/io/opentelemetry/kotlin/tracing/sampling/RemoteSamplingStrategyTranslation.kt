package io.opentelemetry.kotlin.tracing.sampling

import io.opentelemetry.kotlin.error.SdkError
import io.opentelemetry.kotlin.error.SdkErrorHandler
import io.opentelemetry.kotlin.error.SdkErrorSeverity
import io.opentelemetry.kotlin.error.reportError

private const val API = "RemoteSamplingStrategy.toSampler"

/**
 * Translates a parsed remote sampling strategy into a [Sampler].
 *
 * Returns `null` when the strategy cannot be represented by a supported sampler. Callers must then
 * keep their previous sampler rather than replacing it, so a rejected update never silently
 * switches the SDK to a no-op.
 *
 * The result is intentionally *not* wrapped in [ParentBasedSampler]. Per the Trace SDK spec the
 * Jaeger remote sampler is a standalone built-in, like [ProbabilitySampler], and callers who want
 * parent-aware behavior compose it explicitly.
 */
internal fun RemoteSamplingStrategy.toSampler(sdkErrorHandler: SdkErrorHandler): Sampler? {
    warnIfBoundsPresent(sdkErrorHandler)

    if (operationSampling != null) {
        return operationSampling.toSampler(sdkErrorHandler)
    }

    if (probabilisticSampling != null) {
        return probabilisticSampling.toSampler(sdkErrorHandler)
    }

    if (rateLimitingSampling != null) {
        return sdkErrorHandler.warn("rate limiting sampling is not supported")
    }

    return null
}

private fun PerOperationSamplingStrategies.toSampler(sdkErrorHandler: SdkErrorHandler): Sampler? {
    val defaultRate = defaultSamplingProbability
        ?: return sdkErrorHandler.warn("operationSampling is missing defaultSamplingProbability")
    val defaultSampler = rateToSampler(defaultRate, sdkErrorHandler) ?: return null

    val byOperation = mutableMapOf<String, Sampler>()
    for ((operation, probabilisticSampling) in perOperationStrategies) {
        val rate = probabilisticSampling?.samplingRate
            ?: return sdkErrorHandler.warn("'$operation' is missing a samplingRate")
        val sampler = rateToSampler(rate, sdkErrorHandler) ?: return null
        byOperation[operation] = sampler
    }

    return PerOperationSampler(defaultSampler, byOperation)
}

private fun ProbabilisticSamplingStrategy.toSampler(sdkErrorHandler: SdkErrorHandler): Sampler? {
    val rate = samplingRate ?: return sdkErrorHandler.warn("probabilisticSampling is missing a samplingRate")
    return rateToSampler(rate, sdkErrorHandler)
}

/**
 * Maps a sampling rate to a [Sampler]:
 *  - `0` means "never sample" and maps to [AlwaysOffSampler]; [ProbabilitySampler] rejects it.
 *  - any other value is handed to [ProbabilitySampler], whose range check (`[2^-56, 1]`) is caught
 *    here rather than duplicated, so `NaN`, negatives and values above one all degrade to `null`.
 */
private fun rateToSampler(rate: Double, sdkErrorHandler: SdkErrorHandler): Sampler? {
    if (rate == 0.0) {
        return AlwaysOffSampler
    }
    return try {
        ProbabilitySampler(rate, sdkErrorHandler)
    } catch (e: IllegalArgumentException) {
        sdkErrorHandler.warn("sampling rate $rate is not a valid probability")
    }
}

private fun RemoteSamplingStrategy.warnIfBoundsPresent(sdkErrorHandler: SdkErrorHandler) {
    val operation = operationSampling ?: return
    val lowerBound = operation.defaultLowerBoundTracesPerSecond
    val upperBound = operation.defaultUpperBoundTracesPerSecond
    val hasLowerBound = lowerBound != null && lowerBound > 0
    val hasUpperBound = upperBound != null && upperBound > 0
    if (hasLowerBound || hasUpperBound) {
        sdkErrorHandler.reportError(
            SdkError.ApiMisuse(
                api = API,
                message = "per-operation trace bounds are not supported and will be ignored",
                severity = SdkErrorSeverity.WARNING,
            )
        )
    }
}

private fun SdkErrorHandler.warn(message: String): Nothing? {
    reportError(
        SdkError.ApiMisuse(
            api = API,
            message = message,
            severity = SdkErrorSeverity.WARNING,
        )
    )
    return null
}