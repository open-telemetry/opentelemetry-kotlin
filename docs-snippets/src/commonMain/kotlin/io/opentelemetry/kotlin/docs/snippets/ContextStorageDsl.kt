package io.opentelemetry.kotlin.docs.snippets

import io.opentelemetry.kotlin.context.createThreadLocalCurrentContextStorage
import io.opentelemetry.kotlin.createOpenTelemetry

// region context-storage-dsl
val otel = createOpenTelemetry {
    context { storage = createThreadLocalCurrentContextStorage() }
}
// end-region
