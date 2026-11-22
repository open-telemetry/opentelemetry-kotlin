package io.opentelemetry.kotlin.docs.snippets.tracing

import io.opentelemetry.kotlin.tracing.Span

// region tracing-events
fun onPaymentStep(span: Span, step: String) {
    span.addEvent("payment.step") {
        setStringAttribute("payment.step.name", step)
    }
}
// end-region
