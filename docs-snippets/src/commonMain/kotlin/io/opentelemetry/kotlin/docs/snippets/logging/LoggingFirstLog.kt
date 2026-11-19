package io.opentelemetry.kotlin.docs.snippets.logging

import io.opentelemetry.kotlin.OpenTelemetry
import io.opentelemetry.kotlin.logging.SeverityNumber

// region logging-first-log
fun firstLog(otel: OpenTelemetry) {
    // Get a logger once and reuse it. The name identifies the code that emits the logs.
    val logger = otel.loggerProvider.getLogger("com.example.checkout", version = "1.4.0")

    logger.emit(body = "Payment accepted", severityNumber = SeverityNumber.INFO)
}
// end-region
