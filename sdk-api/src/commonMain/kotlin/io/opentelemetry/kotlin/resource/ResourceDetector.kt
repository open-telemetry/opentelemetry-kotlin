package io.opentelemetry.kotlin.resource

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.ThreadSafe
import io.opentelemetry.kotlin.factory.ResourceFactory

/**
 * Detects [Resource] information from the environment that the SDK is running in.
 *
 * Detectors are registered on the SDK via
 * [io.opentelemetry.kotlin.init.ResourceDetectionConfigDsl.detector].
 *
 * https://opentelemetry.io/docs/specs/otel/resource/sdk/#detecting-resource-information-from-the-environment
 */
@ExperimentalApi
@ThreadSafe
public interface ResourceDetector {

    /**
     * A unique name that identifies this detector.
     *
     * Names should be snake case, containing only lowercase alphanumeric and `_` characters, and
     * should reflect the root namespace of the attributes that the detector populates. The names
     * `container`, `host`, `process` and `service` are reserved for detectors defined by the OTel
     * specification.
     */
    public val name: String

    /**
     * Returns the [Resource] detected from the environment, or [ResourceFactory.empty] if nothing
     * could be detected. Failing to detect anything is not considered an error.
     *
     * This is called exactly once while the SDK is being constructed, so implementations should
     * return promptly rather than performing long-running work.
     */
    public fun ResourceFactory.detect(): Resource
}
