package io.opentelemetry.kotlin.instrumentation.ktor.server

import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.ApplicationPlugin
import io.ktor.server.application.createApplicationPlugin
import io.ktor.server.application.hooks.CallFailed
import io.ktor.server.application.hooks.CallSetup
import io.ktor.server.application.hooks.ResponseSent
import io.ktor.server.request.httpMethod
import io.ktor.util.AttributeKey
import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.OpenTelemetry
import io.opentelemetry.kotlin.tracing.Span
import io.opentelemetry.kotlin.tracing.SpanKind

private val spanKey: AttributeKey<Span> =
    AttributeKey("io.opentelemetry.kotlin.instrumentation.ktor.server.span")

/**
 * Creates a Ktor server plugin that records a `SERVER` span for each inbound HTTP request.
 *
 * https://opentelemetry.io/docs/specs/semconv/http/http-spans/
 */
@ExperimentalApi
public fun openTelemetryKtorServerPlugin(openTelemetry: OpenTelemetry): ApplicationPlugin<Unit> =
    createApplicationPlugin("opentelemetry-kotlin-ktor") {
        val tracer = openTelemetry.tracerProvider.getTracer("io.opentelemetry.kotlin.instrumentation.ktor.server")

        on(CallSetup) { call ->
            if (tracer.enabled()) {
                call.attributes.put(
                    spanKey,
                    tracer.startSpan(
                        name = call.request.httpMethod.value,
                        spanKind = SpanKind.SERVER,
                    ),
                )
            }
        }
        on(CallFailed) { call, _ -> call.endSpan() }
        on(ResponseSent) { call -> call.endSpan() }
    }

/**
 * Ends the request's span at most once, by removing it as it is ended.
 */
private fun ApplicationCall.endSpan() {
    val span = attributes.getOrNull(spanKey) ?: return
    attributes.remove(spanKey)
    span.end()
}
