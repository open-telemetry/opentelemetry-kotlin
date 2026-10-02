package io.opentelemetry.kotlin.tracing.export

import io.opentelemetry.kotlin.aliases.OtelJavaExtendedSpanProcessor
import io.opentelemetry.kotlin.aliases.OtelJavaReadWriteSpan
import io.opentelemetry.kotlin.aliases.OtelJavaReadableSpan
import io.opentelemetry.kotlin.aliases.OtelJavaSpanProcessor
import io.opentelemetry.kotlin.awaitOperationResultCode
import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.context.toOtelJavaContext
import io.opentelemetry.kotlin.export.MutableShutdownState
import io.opentelemetry.kotlin.export.OperationResultCode
import io.opentelemetry.kotlin.tracing.model.OtelJavaReadWriteSpanAdapter
import io.opentelemetry.kotlin.tracing.model.OtelJavaReadableSpanAdapter
import io.opentelemetry.kotlin.tracing.model.ReadWriteSpan
import io.opentelemetry.kotlin.tracing.model.ReadWriteSpanAdapter
import io.opentelemetry.kotlin.tracing.model.ReadableSpan
import io.opentelemetry.kotlin.tracing.model.ReadableSpanAdapter

internal class SpanProcessorAdapter(
    private val impl: OtelJavaSpanProcessor
) : SpanProcessor {

    private val shutdownState = MutableShutdownState()
    private val extendedImpl = impl as? OtelJavaExtendedSpanProcessor

    override fun onStart(
        span: ReadWriteSpan,
        parentContext: Context
    ) {
        shutdownState.execute {
            impl.onStart(parentContext.toOtelJavaContext(), span.toOtelJavaReadWriteSpan())
        }
    }

    override fun onEnding(span: ReadWriteSpan) {
        shutdownState.execute {
            extendedImpl?.onEnding(span.toOtelJavaReadWriteSpan())
        }
    }

    override fun onEnd(span: ReadableSpan) {
        shutdownState.execute {
            impl.onEnd(span.toOtelJavaReadableSpan())
        }
    }

    override fun isStartRequired(): Boolean = impl.isStartRequired
    override fun isEndRequired(): Boolean = impl.isEndRequired
    override fun isOnEndingRequired(): Boolean = extendedImpl?.isOnEndingRequired ?: false
    override suspend fun forceFlush(): OperationResultCode =
        awaitOperationResultCode { impl.forceFlush() }

    override suspend fun shutdown(): OperationResultCode =
        shutdownState.shutdown {
            awaitOperationResultCode { impl.shutdown() }
        }

    /**
     * Spans from the compat SDK are unwrapped to the underlying opentelemetry-java span.
     * Spans from the opentelemetry-kotlin SDK are decorated to conform to the Java API.
     */
    private fun ReadWriteSpan.toOtelJavaReadWriteSpan(): OtelJavaReadWriteSpan =
        (this as? ReadWriteSpanAdapter)?.impl ?: OtelJavaReadWriteSpanAdapter(this)

    private fun ReadableSpan.toOtelJavaReadableSpan(): OtelJavaReadableSpan = when (this) {
        is ReadableSpanAdapter -> impl
        is ReadWriteSpanAdapter -> impl
        else -> OtelJavaReadableSpanAdapter(this)
    }
}
