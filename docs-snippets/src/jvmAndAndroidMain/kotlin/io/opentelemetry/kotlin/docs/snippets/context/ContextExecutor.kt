package io.opentelemetry.kotlin.docs.snippets.context

import io.opentelemetry.kotlin.context.currentContext
import io.opentelemetry.kotlin.context.runWithContext
import io.opentelemetry.kotlin.tracing.Tracer
import java.util.concurrent.Executor

// region context-executor
fun runInBackground(executor: Executor, tracer: Tracer) {
    // Read the context on the calling thread...
    val ctx = currentContext()

    executor.execute {
        // ...and make it current on the worker thread.
        ctx.runWithContext {
            tracer.startSpan("background-work").end() // same parent as code on the calling thread
        }
    }
}
// end-region
