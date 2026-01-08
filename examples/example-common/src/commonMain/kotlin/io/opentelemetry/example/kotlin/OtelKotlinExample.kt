@file:OptIn(ExperimentalApi::class)

package io.opentelemetry.example.kotlin

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.OpenTelemetry
import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.createOpenTelemetry
import io.opentelemetry.kotlin.logging.model.SeverityNumber
import io.opentelemetry.kotlin.tracing.Tracer
import io.opentelemetry.kotlin.tracing.model.Span
import io.opentelemetry.kotlin.tracing.model.SpanKind

/**
 * Example of how to instantiate a Kotlin API wrapper around the OTel Java SDK.
 */
fun instantiateOtelApi(url: String?): OpenTelemetry {
    return createOpenTelemetry {
        tracerProvider {
            addSpanProcessor(createSpanProcessor(url))
        }
        loggerProvider {
            addLogRecordProcessor(createLogRecordProcessor(url))
        }
    }
}

fun runTracingExamples(api: OpenTelemetry) {
    // obtain a tracer
    val tracer = api.tracerProvider.getTracer(
        name = "kotlin-example-app",
        version = "0.1.0",
        schemaUrl = "https://example.com/schema.json",
    ) {
        setStringAttribute("example.attribute", "value")
    }

    // create a simple span
    val simpleSpan = tracer.createSpan("simple_span")

    // create a more complex span
    val complexSpan = createComplexSpan(tracer, simpleSpan, api.contextFactory.root())

    // alter the span after its creation

    // 1. add a link to another span
    complexSpan.addLink(simpleSpan.spanContext) {
        // add attributes to the span link
        setLongAttribute("link.attribute", 50L)
    }
    // 2. add a simple event to the span
    complexSpan.addEvent("simple_event")

    // 3. add a complex event to the span
    complexSpan.addEvent(
        name = "complex_event",
        timestamp = 15000005000L
    ) {
        // add attributes to the event
        setBooleanAttribute("event.attribute", true)
    }

    // 4. add an attribute to the span
    complexSpan.setDoubleAttribute("complex_span.attribute", 100.0)

    // end the span
    simpleSpan.end()
    complexSpan.end(timestamp = api.clock.now() + 100)
}

private fun createComplexSpan(
    tracer: Tracer,
    simpleSpan: Span,
    root: Context
): Span = tracer.createSpan(
    name = "complex_span",
    spanKind = SpanKind.CLIENT,
    parentContext = root,
    startTimestamp = 15000000000L,
) {
    // alter the span during its initialization

    // 1. add a link to another span
    addLink(simpleSpan.spanContext) {
        // add attributes to the span link
        setLongAttribute("link.attribute", 50L)
    }
    // 2. add a simple event to the span
    addEvent("simple_event")

    // 3. add a complex event to the span
    addEvent(
        name = "complex_event",
        timestamp = 15000005000L
    ) {
        // add attributes to the event
        setBooleanAttribute("event.attribute", true)
    }

    // 4. add an attribute to the span
    setDoubleAttribute("complex_span.attribute", 100.0)
}

fun runLoggingExamples(api: OpenTelemetry) {
    // obtain a logger
    val logger = api.loggerProvider.getLogger(
        name = "kotlin-example-app",
        version = "0.1.0",
        schemaUrl = "https://example.com/schema.json",
    ) {
        setStringAttribute("example.attribute", "value")
    }

    // create a simple log record
    logger.log("Hello, world!")

    // create a complex log record
    logger.log(
        body = "Complex Log Record",
        timestamp = 15000000003L,
        observedTimestamp = 15000000000L,
        context = null,
        severityNumber = SeverityNumber.WARN,
        severityText = "Warning",
    ) {
        // add attributes to the log record
        setStringAttribute("log.attribute", "value")
    }
}
