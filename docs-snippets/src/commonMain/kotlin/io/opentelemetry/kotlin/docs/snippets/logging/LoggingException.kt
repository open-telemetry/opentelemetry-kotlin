package io.opentelemetry.kotlin.docs.snippets.logging

import io.opentelemetry.kotlin.logging.Logger
import io.opentelemetry.kotlin.logging.SeverityNumber

// region logging-exception
fun chargeCard(logger: Logger, charge: () -> Unit) {
    try {
        charge()
    } catch (e: IllegalStateException) {
        // Adds exception.type, exception.message and exception.stacktrace attributes.
        logger.emit(body = "Payment failed", severityNumber = SeverityNumber.ERROR, exception = e)
    }
}
// end-region
