package io.opentelemetry.kotlin.init

import io.opentelemetry.kotlin.resource.ResourceDetector

internal class CompatResourceDetectionConfig : ResourceDetectionConfigDsl {

    private val registered = mutableListOf<ResourceDetector>()

    internal val detectors: List<ResourceDetector>
        get() = registered.toList()

    override fun detector(detector: ResourceDetector) {
        registered.add(detector)
    }
}
