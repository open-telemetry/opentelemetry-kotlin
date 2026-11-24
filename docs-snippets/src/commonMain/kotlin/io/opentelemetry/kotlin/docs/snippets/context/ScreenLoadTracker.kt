package io.opentelemetry.kotlin.docs.snippets.context

import io.opentelemetry.kotlin.context.Scope
import io.opentelemetry.kotlin.context.currentContext
import io.opentelemetry.kotlin.tracing.Span
import io.opentelemetry.kotlin.tracing.Tracer

// region context-attach-detach
// The work starts in one callback and finishes in another, so it can't fit inside one block.
class ScreenLoadTracker(private val tracer: Tracer) {
    private var span: Span? = null
    private var scope: Scope? = null

    fun onLoadStarted() {
        val newSpan = tracer.startSpan("screen-load")
        span = newSpan
        scope = currentContext().storeSpan(newSpan).attach()
    }

    fun onLoadFinished() {
        scope?.detach() // restore the previous context first...
        span?.end() // ...then end the span.
        scope = null
        span = null
    }
}
// end-region
