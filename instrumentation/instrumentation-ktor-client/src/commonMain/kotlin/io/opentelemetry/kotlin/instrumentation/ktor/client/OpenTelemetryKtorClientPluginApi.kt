package io.opentelemetry.kotlin.instrumentation.ktor.client

import io.ktor.client.plugins.api.ClientPlugin
import io.ktor.client.plugins.api.Send
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.utils.unwrapCancellationException
import io.ktor.http.DEFAULT_PORT
import io.ktor.http.URLBuilder
import io.ktor.http.clone
import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.OpenTelemetry
import io.opentelemetry.kotlin.exceptionType
import io.opentelemetry.kotlin.semconv.ErrorAttributes
import io.opentelemetry.kotlin.semconv.HttpAttributes
import io.opentelemetry.kotlin.semconv.HttpAttributes.HttpRequestMethodValues
import io.opentelemetry.kotlin.semconv.ServerAttributes
import io.opentelemetry.kotlin.semconv.UrlAttributes
import io.opentelemetry.kotlin.tracing.Span
import io.opentelemetry.kotlin.tracing.SpanCreationAction
import io.opentelemetry.kotlin.tracing.SpanKind
import io.opentelemetry.kotlin.tracing.StatusData
import kotlin.coroutines.cancellation.CancellationException

private const val REDACTED = "REDACTED"

private val knownMethods: Set<String> =
    HttpRequestMethodValues.entries.map { it.value }.toSet() - HttpRequestMethodValues.OTHER.value

/**
 * Query parameters whose values are redacted from `url.full`.
 *
 * https://opentelemetry.io/docs/specs/semconv/registry/attributes/url/
 */
private val sensitiveQueryParameters: Set<String> = setOf(
    "X-Amz-Signature",
    "X-Amz-Credential",
    "X-Amz-Security-Token",
    "sig",
    "X-Goog-Signature",
)

/**
 * Creates a Ktor client plugin that records a `CLIENT` span for each outgoing HTTP request.
 *
 * The span follows the stable HTTP client semantic conventions. Its name is the request method, or
 * `HTTP` if the method is not a known method, in which case `http.request.method` is `_OTHER`.
 * `url.full` has any user info and the values of known sensitive query parameters replaced
 * with `REDACTED`.
 *
 * A response with a status code of 400 or above, or below 100, sets the span status to error and
 * `error.type` to the status code. A failed request sets the span status to error and `error.type` to the
 * exception's class name, or `_OTHER` if it has none. On JS the class name is not fully qualified.
 * A cancelled request leaves the span status unset.
 *
 * The span ends when the response is received or the request fails.
 *
 * https://opentelemetry.io/docs/specs/semconv/http/http-spans/
 */
@ExperimentalApi
public fun openTelemetryKtorClientPlugin(openTelemetry: OpenTelemetry): ClientPlugin<Unit> =
    createClientPlugin("opentelemetry-kotlin-ktor") {
        val tracer = openTelemetry.tracerProvider.getTracer("io.opentelemetry.kotlin.instrumentation.ktor.client")

        on(Send) { request ->
            if (tracer.enabled()) {
                val method = request.method.value
                val isKnownMethod = method in knownMethods
                val parentContext = openTelemetry.context.current()
                val span = tracer.startSpan(
                    name = if (isKnownMethod) { method } else { "HTTP" },
                    parentContext = parentContext,
                    spanKind = SpanKind.CLIENT,
                ) {
                    setMethodAttributes(method, isKnownMethod)
                    setUrlAttributes(request.url)
                }
                try {
                    proceed(request).also { call -> span.recordStatusCode(call.response.status.value) }
                } catch (exc: Throwable) {
                    span.recordFailure(exc)
                    throw exc
                } finally {
                    span.end()
                }
            } else {
                proceed(request)
            }
        }
    }

private fun SpanCreationAction.setMethodAttributes(method: String, isKnownMethod: Boolean) {
    if (isKnownMethod) {
        setStringAttribute(HttpAttributes.HTTP_REQUEST_METHOD, method)
    } else {
        setStringAttribute(HttpAttributes.HTTP_REQUEST_METHOD, HttpRequestMethodValues.OTHER.value)
        setStringAttribute(HttpAttributes.HTTP_REQUEST_METHOD_ORIGINAL, method)
    }
}

private fun SpanCreationAction.setUrlAttributes(requestUrl: URLBuilder) {
    // Copy the URL so the request is left unchanged. Unlike URLBuilder.build(), clone() and
    // buildString() don't decode the URL, so they can't throw on malformed percent-encoding.
    val url = requestUrl.clone()
    if (url.encodedUser != null || url.encodedPassword != null) {
        url.encodedUser = REDACTED
        url.encodedPassword = REDACTED
    }
    // buildString() fills in the default host for a relative URL, so read the host after it.
    setStringAttribute(UrlAttributes.URL_FULL, redactQuery(url.buildString()))
    setStringAttribute(ServerAttributes.SERVER_ADDRESS, url.host)

    val port = if (url.port == DEFAULT_PORT) { url.protocol.defaultPort } else { url.port }
    if (port > 0) {
        setLongAttribute(ServerAttributes.SERVER_PORT, port.toLong())
    }
}

private fun redactQuery(url: String): String {
    val queryStart = url.indexOf('?')
    if (queryStart < 0) {
        return url
    }
    val queryEnd = url.indexOf('#', queryStart).takeIf { it >= 0 } ?: url.length
    val query = url.substring(queryStart + 1, queryEnd).split('&').joinToString("&") { param ->
        val name = param.substringBefore('=')
        if (name in sensitiveQueryParameters) { "$name=$REDACTED" } else { param }
    }
    return url.substring(0, queryStart + 1) + query + url.substring(queryEnd)
}

private fun Span.recordStatusCode(statusCode: Int) {
    setLongAttribute(HttpAttributes.HTTP_RESPONSE_STATUS_CODE, statusCode.toLong())
    // A client span is an error for 4xx, 5xx, and any code outside the defined ranges.
    if (statusCode !in 100..399) {
        recordError(statusCode.toString())
    }
}

private fun Span.recordFailure(exc: Throwable) {
    // Ktor reports some failures, such as a request timeout, as the cause of a CancellationException.
    val cause = exc.unwrapCancellationException()
    if (cause !is CancellationException) {
        recordError(cause.exceptionType() ?: ErrorAttributes.ErrorTypeValues.OTHER.value)
    }
}

private fun Span.recordError(errorType: String) {
    setStringAttribute(ErrorAttributes.ERROR_TYPE, errorType)
    setStatus(StatusData.Error(null))
}
