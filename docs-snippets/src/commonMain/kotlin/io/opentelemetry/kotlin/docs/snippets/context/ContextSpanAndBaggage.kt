package io.opentelemetry.kotlin.docs.snippets.context

import io.opentelemetry.kotlin.OpenTelemetry
import io.opentelemetry.kotlin.baggage.createBaggage
import io.opentelemetry.kotlin.context.rootContext

// region context-span-and-baggage
fun spanAndBaggage(otel: OpenTelemetry) {
    val span = otel.tracerProvider.getTracer("checkout").startSpan("checkout")
    val baggage = createBaggage { put("user.tier", "gold") }

    val ctx = rootContext()
        .storeSpan(span)
        .storeBaggage(baggage)

    println(ctx.extractSpan() === span) // true
    println(ctx.extractBaggage().getValue("user.tier")) // gold

    // If nothing is stored you get a safe placeholder, never null.
    val empty = rootContext()
    println(empty.extractSpan().spanContext.isValid) // false: an invalid, non-recording span
    println(empty.extractBaggage().asMap()) // {}: empty baggage

    // clearBaggage() returns a context without any baggage.
    println(ctx.clearBaggage().extractBaggage().asMap()) // {}

    span.end()
}
// end-region
