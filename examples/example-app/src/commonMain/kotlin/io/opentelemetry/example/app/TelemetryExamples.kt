@file:OptIn(ExperimentalApi::class, IncubatingApi::class)

package io.opentelemetry.example.app

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.logging.Logger
import io.opentelemetry.kotlin.logging.model.SeverityNumber
import io.opentelemetry.kotlin.semconv.DbAttributes
import io.opentelemetry.kotlin.semconv.ErrorAttributes
import io.opentelemetry.kotlin.semconv.HttpAttributes
import io.opentelemetry.kotlin.semconv.IncubatingApi
import io.opentelemetry.kotlin.semconv.UrlAttributes
import io.opentelemetry.kotlin.semconv.UserAttributes
import io.opentelemetry.kotlin.tracing.Tracer
import io.opentelemetry.kotlin.tracing.model.SpanKind
import io.opentelemetry.kotlin.tracing.model.SpanRelationships
import kotlinx.coroutines.delay

/**
 * Runs platform-independent examples demonstrating how to use opentelemetry-kotlin's API.
 */
suspend fun runAllExamples(platform: String) {
    println("=== OpenTelemetry Kotlin Example ($platform) ===")
    println()
    if (AppConfig.url != null) {
        println("Telemetry will be exported to: ${AppConfig.url}")
    } else {
        println("Telemetry will be printed to stdout.")
    }
    println()

    val otel = initializeOtelSdk()
    val tracer = otel.tracerProvider.getTracer(AppConfig.APP_NAME)
    val logger = otel.loggerProvider.getLogger(AppConfig.APP_NAME)

    demonstrateBasicSpan(tracer)
    demonstrateComplexSpan(tracer)
    demonstrateSpanNesting(tracer)
    demonstrateBasicLogging(logger)
    demonstrateComplexLogging(logger)

    // flush all pending telemetry before terminating
    AppConfig.spanProcessor.forceFlush()
    AppConfig.logRecordProcessor.forceFlush()
    AppConfig.spanProcessor.shutdown()
    AppConfig.logRecordProcessor.shutdown()

    // give HTTP client time to complete requests before process exits.
    // in future the SDK needs to be updated to handle this automatically.
    delay(500)
}

/**
 * Creates a basic span with no attributes.
 */
private fun demonstrateBasicSpan(tracer: Tracer) {
    val span = tracer.startSpan("basic-span")
    span.end()
}

/**
 * Creates a complex span with attributes and events.
 */
private fun demonstrateComplexSpan(tracer: Tracer) {
    val span = // Add an event to mark when the request started
        tracer.startSpan(
            name = "http-request",
            spanKind = SpanKind.CLIENT,
            // Add an event to mark when the request started
            action = {
                setStringAttribute(HttpAttributes.HTTP_REQUEST_METHOD, "GET")
                setStringAttribute(UrlAttributes.URL_FULL, "https://api.example.com/users/123")
                setLongAttribute("net.peer.port", 443L)

                // Add an event to mark when the request started
                addEvent("request-started")
            })

    // Add more attributes during span lifetime
    span.setLongAttribute(HttpAttributes.HTTP_RESPONSE_STATUS_CODE, 200L)
    span.setDoubleAttribute("http.request.duration_ms", 42.5)

    // Add an event with attributes
    span.addEvent(name = "response-received") {
        setBooleanAttribute("success", true)
        setLongAttribute("bytes_received", 1234L)
    }

    span.end()
}

/**
 * Creates nested spans that represent parent-child relationships.
 */
private fun demonstrateSpanNesting(tracer: Tracer) {
    val parentSpan = tracer.startSpan(
        "parent-operation",
        null,
        SpanKind.INTERNAL,
        null
    ) { setStringAttribute("operation.type", "database-transaction") }

    // Create first child span (database query)
    val childSpan1 =
        tracer.startSpan(
            name = "database-query",
            spanKind = SpanKind.INTERNAL,
            action = {
                setStringAttribute(DbAttributes.DB_SYSTEM_NAME, "postgresql")
                setStringAttribute(DbAttributes.DB_QUERY_TEXT, "SELECT * FROM users WHERE id = ?")
                setStringAttribute(DbAttributes.DB_COLLECTION_NAME, "users")
            })
    childSpan1.end()

    // Create second child span (cache lookup)
    val childSpan2 = tracer.startSpan(
        "cache-lookup",
        null,
        SpanKind.INTERNAL,
        null
    ) { setStringAttribute("cache.type", "redis") }
    childSpan2.end()

    parentSpan.end()
}

/**
 * Emits a basic log.
 */
private fun demonstrateBasicLogging(logger: Logger) {
    logger.emit("Application started successfully")
}

/**
 * Emits complex logs.
 */
private fun demonstrateComplexLogging(logger: Logger) {
    // Info log
    logger.emit(
        body = "User authentication successful",
        severityNumber = SeverityNumber.INFO,
        severityText = "INFO",
        attributes = {
            setStringAttribute(UserAttributes.USER_ID, "user-123")
            setStringAttribute("auth.method", "oauth2")
        })

    // Warning log
    logger.emit(
        body = "Request rate limit approaching threshold",
        severityNumber = SeverityNumber.WARN,
        severityText = "WARN",
        attributes = {
            setStringAttribute("component", "rate-limiter")
            setDoubleAttribute("usage_percentage", 95.0)
        })

    // Error log
    logger.emit(
        body = "Failed to connect to database",
        severityNumber = SeverityNumber.ERROR,
        severityText = "ERROR",
        attributes = {
            setStringAttribute(ErrorAttributes.ERROR_TYPE, "DatabaseConnectionError")
            setStringAttribute(ErrorAttributes.ERROR_MESSAGE, "Connection timeout after 30s")
            setStringAttribute("db.host", "db.example.com")
        })
}
