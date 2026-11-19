package io.opentelemetry.kotlin.docs.snippets.tracing

import io.opentelemetry.kotlin.OpenTelemetrySdk
import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.createOpenTelemetry
import io.opentelemetry.kotlin.export.OperationResultCode
import io.opentelemetry.kotlin.semconv.IncubatingApi
import io.opentelemetry.kotlin.semconv.SessionAttributes
import io.opentelemetry.kotlin.tracing.export.SpanProcessor
import io.opentelemetry.kotlin.tracing.export.batchSpanProcessor
import io.opentelemetry.kotlin.tracing.export.compositeSpanProcessor
import io.opentelemetry.kotlin.tracing.export.otlpHttpSpanExporter
import io.opentelemetry.kotlin.tracing.model.ReadWriteSpan
import io.opentelemetry.kotlin.tracing.model.ReadableSpan

// region tracing-custom-processor
@OptIn(IncubatingApi::class)
class SessionIdSpanProcessor(private val sessionId: () -> String) : SpanProcessor {
    override fun onStart(span: ReadWriteSpan, parentContext: Context) {
        span.setStringAttribute(SessionAttributes.SESSION_ID, sessionId())
    }

    override fun onEnding(span: ReadWriteSpan) = Unit
    override fun onEnd(span: ReadableSpan) = Unit

    // Lets the SDK skip the callbacks that do nothing.
    override fun isOnEndingRequired() = false
    override fun isEndRequired() = false

    override suspend fun forceFlush() = OperationResultCode.Success
    override suspend fun shutdown() = OperationResultCode.Success
}

fun initTracingWithProcessor(sessionId: () -> String): OpenTelemetrySdk = createOpenTelemetry {
    tracerProvider {
        export {
            compositeSpanProcessor(
                SessionIdSpanProcessor(sessionId), // runs first, so every span gets the attribute
                batchSpanProcessor(otlpHttpSpanExporter { endpoint = "http://localhost:4318" }),
            )
        }
    }
}
// end-region
