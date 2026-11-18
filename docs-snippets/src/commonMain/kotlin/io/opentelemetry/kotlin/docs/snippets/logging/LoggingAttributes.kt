package io.opentelemetry.kotlin.docs.snippets.logging

import io.opentelemetry.kotlin.logging.Logger
import io.opentelemetry.kotlin.logging.SeverityNumber

// region logging-attributes
fun logPayment(logger: Logger, cartId: String, items: Long) {
    logger.emit(
        body = "Payment accepted",
        severityNumber = SeverityNumber.INFO,
        severityText = "INFO",
    ) {
        setStringAttribute("cart.id", cartId)
        setLongAttribute("cart.items", items)
    }
}
// end-region
