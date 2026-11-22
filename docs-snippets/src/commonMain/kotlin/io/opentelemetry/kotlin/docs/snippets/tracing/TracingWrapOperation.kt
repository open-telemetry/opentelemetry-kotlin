package io.opentelemetry.kotlin.docs.snippets.tracing

import io.opentelemetry.kotlin.tracing.StatusData
import io.opentelemetry.kotlin.tracing.Tracer
import io.opentelemetry.kotlin.tracing.wrapOperation

// region tracing-wrap-operation
fun chargeCard(tracer: Tracer, charge: () -> Unit) {
    // Ends the span when the block returns. If the block throws, the exception is recorded on
    // the span, the status is set to Error and the exception is rethrown.
    tracer.startSpan("charge-card").wrapOperation {
        charge()
        StatusData.Ok
    }
}
// end-region
