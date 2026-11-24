package io.opentelemetry.kotlin.docs.snippets.logging

import io.opentelemetry.kotlin.logging.Logger
import io.opentelemetry.kotlin.logging.SeverityNumber

// region logging-timestamp
// Forwards a line read from a log file, which records when it was originally written.
fun forwardLine(logger: Logger, line: String, writtenAtMs: Long) {
    logger.emit(
        body = line,
        timestamp = writtenAtMs * 1_000_000, // nanoseconds since the Unix epoch
        severityNumber = SeverityNumber.WARN,
        severityText = "W", // the severity as the source wrote it
    )
}
// end-region
