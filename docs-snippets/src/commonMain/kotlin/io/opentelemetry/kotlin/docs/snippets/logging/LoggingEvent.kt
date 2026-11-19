package io.opentelemetry.kotlin.docs.snippets.logging

import io.opentelemetry.kotlin.logging.Logger
import io.opentelemetry.kotlin.semconv.AppAttributes
import io.opentelemetry.kotlin.semconv.IncubatingApi

// region logging-event
@OptIn(IncubatingApi::class)
fun recordButtonTap(logger: Logger) {
    logger.emit(eventName = "app.widget.click") {
        setStringAttribute(AppAttributes.APP_WIDGET_ID, "checkout")
    }
}
// end-region
