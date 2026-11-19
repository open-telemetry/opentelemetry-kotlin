package io.opentelemetry.kotlin.docs.snippets.tracing

import io.opentelemetry.kotlin.OpenTelemetrySdk
import io.opentelemetry.kotlin.createOpenTelemetry

// region tracing-sampler
fun initTracingWithSampler(): OpenTelemetrySdk = createOpenTelemetry {
    tracerProvider {
        sampler {
            // Keep 10% of new traces. Spans with a parent follow the parent's decision.
            parentBased(root = probability(0.1))
        }
    }
}
// end-region
