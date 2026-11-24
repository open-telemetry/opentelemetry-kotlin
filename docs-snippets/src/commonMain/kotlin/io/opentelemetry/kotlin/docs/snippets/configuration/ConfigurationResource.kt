package io.opentelemetry.kotlin.docs.snippets.configuration

import io.opentelemetry.kotlin.OpenTelemetrySdk
import io.opentelemetry.kotlin.createOpenTelemetry

// region configuration-resource
fun initWithResource(): OpenTelemetrySdk = createOpenTelemetry {
    serviceName = "checkout-service" // wins over any service.name set below
    resource(schemaUrl = "https://opentelemetry.io/schemas/1.37.0") {
        setStringAttribute("service.version", "2.4.1")
        setStringAttribute("deployment.environment.name", "staging")
    }
    tracerProvider {
        // Merged on top of the resource above, for spans only.
        resource(mapOf("telemetry.pipeline" to "traces"))
    }
}
// end-region
