package io.opentelemetry.kotlin.docs.snippets.configuration

import io.opentelemetry.kotlin.OpenTelemetrySdk
import io.opentelemetry.kotlin.createOpenTelemetry
import io.opentelemetry.kotlin.factory.ResourceFactory
import io.opentelemetry.kotlin.resource.Resource
import io.opentelemetry.kotlin.resource.ResourceDetector
import io.opentelemetry.kotlin.resource.deviceResourceDetector
import io.opentelemetry.kotlin.resource.hostResourceDetector

// region configuration-resource-detection
// A custom detector. It runs once, while the SDK is being created.
private object BuildInfoDetector : ResourceDetector {
    override val name: String = "build_info"

    override fun ResourceFactory.detect(): Resource = create {
        setStringAttribute("app.build.flavor", "beta")
    }
}

fun initWithDetectors(): OpenTelemetrySdk = createOpenTelemetry {
    resourceDetection {
        detector(hostResourceDetector())
        detector(deviceResourceDetector())
        detector(BuildInfoDetector) // later detectors win on key collisions
    }
}
// end-region
