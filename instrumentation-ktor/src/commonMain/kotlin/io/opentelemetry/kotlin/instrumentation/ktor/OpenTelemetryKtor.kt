package io.opentelemetry.kotlin.instrumentation.ktor

import io.ktor.client.plugins.api.ClientPlugin
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.contentLength
import io.ktor.http.content.OutgoingContent
import io.ktor.util.AttributeKey
import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.OpenTelemetry
import io.opentelemetry.kotlin.semconv.HttpAttributes
import io.opentelemetry.kotlin.semconv.ServerAttributes
import io.opentelemetry.kotlin.semconv.UrlAttributes
import io.opentelemetry.kotlin.tracing.data.StatusData
import io.opentelemetry.kotlin.tracing.model.Span
import io.opentelemetry.kotlin.tracing.model.SpanKind
import io.opentelemetry.kotlin.tracing.recordException

/**
 * Configuration for the [OpenTelemetryKtor] plugin.
 */
@ExperimentalApi
public class OpenTelemetryKtorConfig {
    /**
     * The [OpenTelemetry] instance to use for instrumentation.
     */
    public var openTelemetry: OpenTelemetry? = null
}

private val OpenTelemetrySpanKey = AttributeKey<Span>("OpenTelemetrySpan")

/**
 * A Ktor [ClientPlugin] that provides OpenTelemetry instrumentation for outgoing HTTP requests.
 *
 * Example usage:
 * ```kotlin
 * val client = HttpClient {
 *     install(OpenTelemetryKtor) {
 *         openTelemetry = myOpenTelemetryInstance
 *     }
 * }
 * ```
 */
@ExperimentalApi
public val OpenTelemetryKtor: ClientPlugin<OpenTelemetryKtorConfig> = createClientPlugin(
    "OpenTelemetryKtor",
    ::OpenTelemetryKtorConfig
) {
    val otel = pluginConfig.openTelemetry ?: error("OpenTelemetry must be provided")
    val tracer = otel.tracerProvider.getTracer("io.opentelemetry.kotlin.ktor")

    on(io.ktor.client.plugins.api.Send) { request ->
        val spanName = request.method.value
        val span = tracer.startSpan(
            name = spanName,
            spanKind = SpanKind.CLIENT
        ) {
            setStringAttribute(HttpAttributes.HTTP_REQUEST_METHOD, request.method.value)
            setStringAttribute(UrlAttributes.URL_FULL, request.url.buildString())
            setStringAttribute(ServerAttributes.SERVER_ADDRESS, request.url.host)
            
            val port = if (request.url.port != request.url.protocol.defaultPort) {
                request.url.port.toLong()
            } else {
                request.url.protocol.defaultPort.toLong()
            }
            setLongAttribute(ServerAttributes.SERVER_PORT, port)
            
            setStringAttribute(UrlAttributes.URL_SCHEME, request.url.protocol.name)
            
            request.body.let { 
                if (it is OutgoingContent) {
                    it.contentLength?.let { length ->
                        setLongAttribute(HttpAttributes.HTTP_REQUEST_BODY_SIZE, length)
                    }
                }
            }
        }

        request.attributes.put(OpenTelemetrySpanKey, span)

        try {
            val call = proceed(request)
            val response = call.response
            
            span.setLongAttribute(HttpAttributes.HTTP_RESPONSE_STATUS_CODE, response.status.value.toLong())
            response.contentLength()?.let { length ->
                setLongAttribute(HttpAttributes.HTTP_RESPONSE_BODY_SIZE, length)
            }
            
            if (response.status.value >= 400) {
                span.status = StatusData.Error(response.status.description)
            } else {
                span.status = StatusData.Ok
            }
            
            span.end()
            call
        } catch (e: Throwable) {
            span.recordException(e)
            span.status = StatusData.Error(e.message)
            span.end()
            throw e
        }
    }
}
