package io.opentelemetry.kotlin.docs.snippets

import io.opentelemetry.kotlin.OpenTelemetry
import io.opentelemetry.kotlin.context.ContextKey
import io.opentelemetry.kotlin.context.createContextKey
import io.opentelemetry.kotlin.context.currentContext
import io.opentelemetry.kotlin.context.runWithContext

// region context-usage
// Declare keys once and share the instance with whatever needs to read the value back.
// A key with a matching name is a different key - see the ContextKey docs.
private val USER_ID: ContextKey<String> = createContextKey("user.id")

internal fun exampleUsage(api: OpenTelemetry) {
    // 1. obtain the current context. Defaults to rootContext() if no current context is available.
    val ctx = currentContext()

    // 2. create a new context but don't set it as the current context.
    val newContext = ctx.set(USER_ID, "abc123")

    // 3. set the new context as the current context while the block runs. The previous context
    // is restored afterwards, even if the block throws.
    newContext.runWithContext {
        // 4. perform tracing/logging here. tracers/loggers read currentContext() internally, so
        // the span is parented to whatever span newContext holds.
        api.tracerProvider.getTracer("tracer").startSpan("my_span").end()
    }
}
// end-region
