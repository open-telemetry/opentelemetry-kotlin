package io.opentelemetry.kotlin.docs.snippets.logging

import io.opentelemetry.kotlin.logging.Logger

// region logging-event
fun recordButtonTap(logger: Logger) {
    logger.emit(eventName = "com.example.button.tap") {
        setStringAttribute("button.id", "checkout")
    }
}
// end-region
