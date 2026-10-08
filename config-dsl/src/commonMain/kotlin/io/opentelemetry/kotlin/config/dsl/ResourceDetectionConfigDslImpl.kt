package io.opentelemetry.kotlin.config.dsl

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.init.ResourceDetectionConfigDsl
import io.opentelemetry.kotlin.resource.ResourceDetector

/**
 * Captures the resource detectors registered programmatically, in registration order.
 */
@ExperimentalApi
class ResourceDetectionConfigDslImpl : ResourceDetectionConfigDsl {

    private val registered = mutableListOf<ResourceDetector>()

    val detectors: List<ResourceDetector>
        get() = registered.toList()

    override fun detector(detector: ResourceDetector) {
        registered.add(detector)
    }
}
