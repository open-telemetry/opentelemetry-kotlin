package io.opentelemetry.kotlin.tracing.sampling

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.error.SdkErrorHandler
import io.opentelemetry.kotlin.error.guardOrDefault
import io.opentelemetry.kotlin.init.SamplerConfigDsl

/**
 * Builds a sampler from a Jaeger remote sampling strategy JSON response.
 *
 * Returns null for malformed or unsupported strategies. Callers can keep their current sampler
 * when this happens. Fetching the response and scheduling updates are the caller's responsibility.
 */
@ExperimentalApi
public fun SamplerConfigDsl.remoteSamplerFromJson(json: String, sdkErrorHandler: SdkErrorHandler): Sampler? =
    sdkErrorHandler.guardOrDefault(null, "Failed to build a sampler from a Jaeger strategy") {
        parseRemoteSamplingStrategy(json, sdkErrorHandler)?.toSampler(sdkErrorHandler) { rate ->
            if (rate == 0.0) {
                alwaysOff()
            } else {
                probability(rate)
            }
        }
    }
