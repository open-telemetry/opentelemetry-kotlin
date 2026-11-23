package io.opentelemetry.kotlin.docs.snippets.configuration

import io.opentelemetry.kotlin.OpenTelemetrySdk
import io.opentelemetry.kotlin.createOpenTelemetry

// region configuration-sampler
fun initWithSampler(): OpenTelemetrySdk = createOpenTelemetry {
    tracerProvider {
        // Keep 10% of new traces, and follow the parent's decision for everything else.
        sampler { parentBased(root = probability(0.1)) }
    }
}
// end-region
