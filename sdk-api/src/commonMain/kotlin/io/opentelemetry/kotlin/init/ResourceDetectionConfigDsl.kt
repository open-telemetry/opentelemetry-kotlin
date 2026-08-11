package io.opentelemetry.kotlin.init

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.resource.ResourceDetector

/**
 * Defines configuration for detecting resource information from the environment.
 *
 * https://opentelemetry.io/docs/specs/otel/resource/sdk/#detecting-resource-information-from-the-environment
 */
@ExperimentalApi
@ConfigDsl
public interface ResourceDetectionConfigDsl {

    /**
     * Registers a [ResourceDetector]. Detectors run once, in registration order, while the SDK is
     * being constructed. Where two detectors set the same attribute key the later one wins:
     *
     * ```
     * resourceDetection {
     *     detector(a) // sets host.name to "a"
     *     detector(b) // sets host.name to "b"
     * }
     * // host.name is "b". Swapping the two lines makes it "a".
     * ```
     *
     * Anything declared via [ResourceConfigDsl.resource] or [ResourceConfigDsl.serviceName] takes
     * precedence over detected values, regardless of registration order.
     */
    public fun detector(detector: ResourceDetector)
}
