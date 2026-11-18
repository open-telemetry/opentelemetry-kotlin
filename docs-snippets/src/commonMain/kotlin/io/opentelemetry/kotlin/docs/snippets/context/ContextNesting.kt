package io.opentelemetry.kotlin.docs.snippets.context

import io.opentelemetry.kotlin.context.currentContext
import io.opentelemetry.kotlin.context.runWithContext
import io.opentelemetry.kotlin.tracing.Tracer

// region context-nesting
fun nesting(tracer: Tracer) {
    val parent = tracer.startSpan("parent")
    currentContext().storeSpan(parent).runWithContext {
        // startSpan() reads currentContext(), so "child" is a child of "parent".
        val child = tracer.startSpan("child")
        currentContext().storeSpan(child).runWithContext {
            tracer.startSpan("grandchild").end() // a child of "child"
        }
        child.end()
        // "parent" is the current span again here.
    }
    parent.end()
}
// end-region
