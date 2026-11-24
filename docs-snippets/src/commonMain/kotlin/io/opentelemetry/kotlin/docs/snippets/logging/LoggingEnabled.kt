package io.opentelemetry.kotlin.docs.snippets.logging

import io.opentelemetry.kotlin.logging.Logger
import io.opentelemetry.kotlin.logging.SeverityNumber

// region logging-enabled
fun logCart(logger: Logger, cart: List<String>) {
    // Skip building an expensive body if the log would be dropped anyway.
    if (logger.enabled(severityNumber = SeverityNumber.DEBUG)) {
        logger.emit(body = cart.joinToString(), severityNumber = SeverityNumber.DEBUG)
    }
}
// end-region
