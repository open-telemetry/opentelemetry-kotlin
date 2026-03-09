package io.opentelemetry.kotlin.tracing.export

import io.opentelemetry.kotlin.aliases.OtelJavaSpanProcessor
import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.context.toOtelJavaContext
import io.opentelemetry.kotlin.export.MutableShutdownState
import io.opentelemetry.kotlin.export.OperationResultCode
import io.opentelemetry.kotlin.toOperationResultCode
import io.opentelemetry.kotlin.tracing.model.ReadWriteSpan
import io.opentelemetry.kotlin.tracing.model.ReadWriteSpanAdapter
import io.opentelemetry.kotlin.tracing.model.ReadableSpan
import io.opentelemetry.kotlin.tracing.model.ReadableSpanAdapter

internal class SpanProcessorAdapter(
    private val impl: OtelJavaSpanProcessor
) : SpanProcessor {

    private val shutdownState = MutableShutdownState()

    override fun onStart(
        span: ReadWriteSpan,
        parentContext: Context
    ) {
        shutdownState.execute {
            if (span is ReadWriteSpanAdapter) {
                impl.onStart(parentContext.toOtelJavaContext(), span.impl)
            }
        }
    }

    override fun onEnding(span: ReadWriteSpan) {
        // no-op - unsupported in opentelemetry-java
    }

    override fun onEnd(span: ReadableSpan) {
        shutdownState.execute {
            if (span is ReadableSpanAdapter) {
                impl.onEnd(span.impl)
            }
        }
    }

    override fun isStartRequired(): Boolean = impl.isStartRequired
    override fun isEndRequired(): Boolean = impl.isEndRequired
    override suspend fun forceFlush(): OperationResultCode = impl.forceFlush().toOperationResultCode()

    override suspend fun shutdown(): OperationResultCode =
        shutdownState.shutdown {
            impl.shutdown().toOperationResultCode()
        }
}
