package io.opentelemetry.kotlin.docs.snippets.logging

import io.opentelemetry.kotlin.logging.Logger
import io.opentelemetry.kotlin.logging.SeverityNumber

// region logging-structured-body
fun logStructured(logger: Logger) {
    logger.emit(
        body = mapOf(
            "message" to "Payment accepted",
            "amount" to 42.5,
            "items" to listOf("sku-1", "sku-2"),
        ),
        severityNumber = SeverityNumber.INFO,
    )
}
// end-region
