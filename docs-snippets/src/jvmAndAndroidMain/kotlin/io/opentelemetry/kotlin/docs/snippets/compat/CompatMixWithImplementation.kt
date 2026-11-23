package io.opentelemetry.kotlin.docs.snippets.compat

import io.opentelemetry.kotlin.context.createOtelJavaCurrentContextStorage
import io.opentelemetry.kotlin.createOpenTelemetry
import io.opentelemetry.kotlin.toOtelJavaApi
import io.opentelemetry.api.OpenTelemetry as JavaOpenTelemetry

// region compat-mix-with-implementation
fun javaApiForImplementation(): JavaOpenTelemetry {
    val otel = createOpenTelemetry {
        // Keep the current context in opentelemetry-java's ContextStorage.
        context { storage { createOtelJavaCurrentContextStorage() } }
    }
    return otel.toOtelJavaApi()
}
// end-region
