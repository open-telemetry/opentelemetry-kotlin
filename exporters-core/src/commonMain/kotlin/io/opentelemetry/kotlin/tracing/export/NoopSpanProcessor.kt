package io.opentelemetry.kotlin.tracing.export

import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.export.OperationResultCode
import io.opentelemetry.kotlin.tracing.model.ReadWriteSpan
import io.opentelemetry.kotlin.tracing.model.ReadableSpan

internal object NoopSpanProcessor : SpanProcessor {
    override fun onStart(span: ReadWriteSpan, parentContext: Context) = Unit
    override fun onEnding(span: ReadWriteSpan) = Unit
    override fun onEnd(span: ReadableSpan) = Unit
    override fun isStartRequired(): Boolean = false
    override fun isEndRequired(): Boolean = false
    override suspend fun forceFlush(): OperationResultCode = OperationResultCode.Success
    override suspend fun shutdown(): OperationResultCode = OperationResultCode.Success
}
