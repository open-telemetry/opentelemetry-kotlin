package io.opentelemetry.kotlin.tracing.sampling

import io.opentelemetry.kotlin.error.SdkError
import io.opentelemetry.kotlin.error.SdkErrorHandler
import io.opentelemetry.kotlin.error.SdkErrorSeverity
import io.opentelemetry.kotlin.error.reportError

private const val MIN_REMOTE_SAMPLING_RATE = 1.0 / (1L shl 56)
private const val API = "RemoteSamplingStrategy.toSampler"

/**
 * Translates a parsed remote sampling strategy into a [Sampler].
 *
 * Returns `null` when the strategy cannot be represented by a supported sampler. Callers must then
 * keep their previous sampler rather than replacing it, so a rejected update never silently
 * switches the SDK to a no-op.
 *
 * The result is intentionally *not* wrapped in a parent-based sampler. Per the Trace SDK spec the
 * Jaeger remote sampler is a standalone built-in, like a probability sampler, and callers who want
 * parent-aware behavior compose it explicitly.
 */
internal fun RemoteSamplingStrategy.toSampler(
    sdkErrorHandler: SdkErrorHandler,
    samplerForRate: (Double) -> Sampler,
): Sampler? {
    warnIfBoundsPresent(sdkErrorHandler)

    if (operationSampling != null) {
        return operationSampling.toSampler(sdkErrorHandler, samplerForRate)
    }

    if (probabilisticSampling != null) {
        return probabilisticSampling.toSampler(sdkErrorHandler, samplerForRate)
    }

    if (rateLimitingSampling != null) {
        return sdkErrorHandler.warn("rate limiting sampling is not supported")
    }

    return null
}

private fun PerOperationSamplingStrategies.toSampler(
    sdkErrorHandler: SdkErrorHandler,
    samplerForRate: (Double) -> Sampler,
): Sampler? {
    val defaultRate = defaultSamplingProbability
        ?: return sdkErrorHandler.warn("operationSampling is missing defaultSamplingProbability")
    val defaultSampler = rateToSampler(defaultRate, sdkErrorHandler, samplerForRate) ?: return null

    val byOperation = mutableMapOf<String, Sampler>()
    for ((operation, probabilisticSampling) in perOperationStrategies) {
        val rate = probabilisticSampling?.samplingRate
            ?: return sdkErrorHandler.warn("'$operation' is missing a samplingRate")
        val sampler = rateToSampler(rate, sdkErrorHandler, samplerForRate) ?: return null
        byOperation[operation] = sampler
    }

    return PerOperationSampler(defaultSampler, byOperation)
}

private fun ProbabilisticSamplingStrategy.toSampler(
    sdkErrorHandler: SdkErrorHandler,
    samplerForRate: (Double) -> Sampler,
): Sampler? {
    val rate = samplingRate ?: return sdkErrorHandler.warn("probabilisticSampling is missing a samplingRate")
    return rateToSampler(rate, sdkErrorHandler, samplerForRate)
}

private fun rateToSampler(
    rate: Double,
    sdkErrorHandler: SdkErrorHandler,
    samplerForRate: (Double) -> Sampler,
): Sampler? {
    // Zero is valid. Positive rates below 2^-56 cannot be represented by the sampler.
    if (rate != 0.0 && rate !in MIN_REMOTE_SAMPLING_RATE..1.0) {
        return sdkErrorHandler.warn("sampling rate $rate is not a valid probability")
    }
    return samplerForRate(rate)
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
