package io.opentelemetry.kotlin.tracing.export

import io.opentelemetry.kotlin.aliases.OtelJavaContext
import io.opentelemetry.kotlin.aliases.OtelJavaExtendedSpanProcessor
import io.opentelemetry.kotlin.aliases.OtelJavaReadWriteSpan
import io.opentelemetry.kotlin.aliases.OtelJavaReadableSpan
import io.opentelemetry.kotlin.aliases.OtelJavaSpanProcessor
import io.opentelemetry.kotlin.context.toOtelKotlinContext
import io.opentelemetry.kotlin.error.SdkErrorHandler
import io.opentelemetry.kotlin.error.guard
import io.opentelemetry.kotlin.error.guardOrDefault
import io.opentelemetry.kotlin.tracing.model.ReadWriteSpanAdapter
import io.opentelemetry.kotlin.tracing.model.ReadableSpanAdapter
import io.opentelemetry.sdk.trace.ReadWriteSpan

internal class OtelJavaSpanProcessorAdapter(
    private val impl: SpanProcessor,
    private val sdkErrorHandler: SdkErrorHandler,
) : OtelJavaSpanProcessor, OtelJavaExtendedSpanProcessor {

    override fun onStart(parentContext: OtelJavaContext, span: OtelJavaReadWriteSpan) {
        sdkErrorHandler.guard("SpanProcessor.onStart failed") {
            impl.onStart(ReadWriteSpanAdapter(span), parentContext.toOtelKotlinContext())
        }
    }

    override fun onEnd(span: OtelJavaReadableSpan) {
        sdkErrorHandler.guard("SpanProcessor.onEnd failed") {
            impl.onEnd(ReadableSpanAdapter(span))
        }
    }

    override fun onEnding(span: ReadWriteSpan) {
        sdkErrorHandler.guard("SpanProcessor.onEnding failed") {
            impl.onEnding(ReadWriteSpanAdapter(span))
        }
    }

    override fun isStartRequired(): Boolean =
        sdkErrorHandler.guardOrDefault(true, "SpanProcessor.isStartRequired failed") { impl.isStartRequired() }

    override fun isEndRequired(): Boolean =
        sdkErrorHandler.guardOrDefault(true, "SpanProcessor.isEndRequired failed") { impl.isEndRequired() }

    override fun isOnEndingRequired(): Boolean =
        sdkErrorHandler.guardOrDefault(true, "SpanProcessor.isOnEndingRequired failed") { impl.isOnEndingRequired() }
}
