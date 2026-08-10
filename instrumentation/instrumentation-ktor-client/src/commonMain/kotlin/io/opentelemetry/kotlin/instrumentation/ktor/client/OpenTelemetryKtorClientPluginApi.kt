package io.opentelemetry.kotlin.instrumentation.ktor.client

import io.ktor.client.plugins.api.ClientPlugin
import io.ktor.client.plugins.api.Send
import io.ktor.client.plugins.api.createClientPlugin
import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.OpenTelemetry
import io.opentelemetry.kotlin.tracing.SpanKind

/**
 * Creates a Ktor client plugin that records a `CLIENT` span for each outgoing HTTP request.
 *
 * https://opentelemetry.io/docs/specs/semconv/http/http-spans/
 */
@ExperimentalApi
public fun openTelemetryKtorClientPlugin(openTelemetry: OpenTelemetry): ClientPlugin<Unit> =
    createClientPlugin("opentelemetry-kotlin-ktor") {
        val tracer = openTelemetry.tracerProvider.getTracer("io.opentelemetry.kotlin.instrumentation.ktor.client")

        on(Send) { request ->
            if (tracer.enabled()) {
                val parentContext = openTelemetry.context.implicit()
                val span = tracer.startSpan(
                    name = request.method.value,
                    parentContext = parentContext,
                    spanKind = SpanKind.CLIENT,
                )
                try {
                    proceed(request)
                } finally {
                    span.end()
                }
            } else {
                proceed(request)
            }
        }
    }
