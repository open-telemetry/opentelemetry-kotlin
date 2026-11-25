package io.opentelemetry.kotlin.docs.snippets.propagation

import io.opentelemetry.kotlin.OpenTelemetry
import io.opentelemetry.kotlin.context.currentContext

// region propagation-baggage
fun sendWithBaggage(otel: OpenTelemetry, request: OutgoingRequest) {
    // Add to the baggage that is already current rather than replacing it.
    val baggage = currentContext().baggage.put("user.tier", "gold")
    val ctx = currentContext().storeBaggage(baggage)

    otel.propagator.inject(ctx, request, outgoingRequestSetter)
    println(request.headers()["baggage"]) // user.tier=gold
}
// end-region
