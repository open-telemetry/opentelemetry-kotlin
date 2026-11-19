package io.opentelemetry.kotlin.docs.snippets.context

import io.opentelemetry.kotlin.context.asCoroutineContextElement
import io.opentelemetry.kotlin.context.currentContext
import io.opentelemetry.kotlin.tracing.Tracer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

// region context-coroutines
// The SDK must use thread-local storage:
// createOpenTelemetry { context { storage { createThreadLocalCurrentContextStorage() } } }
suspend fun loadProfile(tracer: Tracer) {
    val span = tracer.startSpan("load-profile")
    val ctx = currentContext().storeSpan(span)

    withContext(Dispatchers.IO + ctx.asCoroutineContextElement()) {
        delay(100) // suspend, then resume, possibly on another thread

        // ctx is still current here, so this is a child of "load-profile".
        tracer.startSpan("parse-profile").end()
    }
    span.end()
}
// end-region
