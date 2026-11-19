package io.opentelemetry.kotlin.docs.snippets.context

import io.opentelemetry.kotlin.OpenTelemetry
import io.opentelemetry.kotlin.context.rootContext
import io.opentelemetry.kotlin.tracing.Span

// region context-explicit-parent
fun explicitParent(otel: OpenTelemetry, parentSpan: Span) {
    val tracer = otel.tracerProvider.getTracer("example")
    val logger = otel.loggerProvider.getLogger("example")
    val ctx = rootContext().storeSpan(parentSpan)

    // The span's parent is parentSpan, whatever the current context is.
    tracer.startSpan("child", parentContext = ctx).end()

    // The log record is linked to parentSpan.
    logger.emit(body = "child finished", context = ctx)

    // Passing the root context starts a brand new trace.
    tracer.startSpan("unrelated-work", parentContext = rootContext()).end()
}
// end-region
