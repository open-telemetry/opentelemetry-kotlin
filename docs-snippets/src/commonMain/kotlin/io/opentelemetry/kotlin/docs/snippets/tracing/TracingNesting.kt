package io.opentelemetry.kotlin.docs.snippets.tracing

import io.opentelemetry.kotlin.context.currentContext
import io.opentelemetry.kotlin.context.runWithContext
import io.opentelemetry.kotlin.tracing.Tracer

// region tracing-nesting
fun placeOrder(tracer: Tracer) {
    val order = tracer.startSpan("place-order")
    val ctx = currentContext().storeSpan(order)

    // Spans started while "place-order" is in the current context are its children.
    ctx.runWithContext {
        tracer.startSpan("reserve-stock").end()
    }

    // Or pass the parent explicitly, e.g. from a callback that runs on another thread.
    tracer.startSpan("send-receipt", parentContext = ctx).end()

    order.end()
}
// end-region
