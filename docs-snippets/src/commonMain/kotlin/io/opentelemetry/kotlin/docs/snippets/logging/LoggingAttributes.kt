package io.opentelemetry.kotlin.docs.snippets.logging

import io.opentelemetry.kotlin.logging.Logger
import io.opentelemetry.kotlin.logging.SeverityNumber
import io.opentelemetry.kotlin.semconv.AppAttributes
import io.opentelemetry.kotlin.semconv.IncubatingApi

// region logging-attributes
@OptIn(IncubatingApi::class)
fun logPayment(logger: Logger, cartId: String, items: Long) {
    logger.emit(
        body = "Payment accepted",
        severityNumber = SeverityNumber.INFO,
        severityText = "INFO",
    ) {
        setStringAttribute(AppAttributes.APP_SCREEN_NAME, "checkout") // a semantic convention key
        setStringAttribute("cart.id", cartId) // a custom key
        setLongAttribute("cart.items", items)
    }
}
// end-region
