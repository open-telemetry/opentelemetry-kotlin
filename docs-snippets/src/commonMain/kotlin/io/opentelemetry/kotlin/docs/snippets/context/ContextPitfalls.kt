package io.opentelemetry.kotlin.docs.snippets.context

import io.opentelemetry.kotlin.context.currentContext
import io.opentelemetry.kotlin.context.runWithContext
import io.opentelemetry.kotlin.tracing.Tracer

// region context-pitfalls
fun pitfalls(tracer: Tracer) {
    val span = tracer.startSpan("work")

    // Don't: storeSpan() returns a new context. On its own it doesn't make anything current.
    // currentContext().storeSpan(span)

    // Don't: attach() without detach() leaves the span current after the work has finished.
    // currentContext().storeSpan(span).attach()

    // Do: make the span current for exactly as long as the work runs.
    currentContext().storeSpan(span).runWithContext {
        doWork()
    }
    span.end()
}
// end-region

private fun doWork() = Unit
