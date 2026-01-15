package io.opentelemetry.kotlin.tracing.export

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.export.OperationResultCode
import io.opentelemetry.kotlin.tracing.model.ReadWriteSpan
import io.opentelemetry.kotlin.tracing.model.ReadableSpan

@OptIn(ExperimentalApi::class)
class FakeSpanProcessor(
    var startRequired: Boolean = true,
    var endRequired: Boolean = true,
    var flushCode: () -> OperationResultCode = { OperationResultCode.Success },
    var shutdownCode: () -> OperationResultCode = { OperationResultCode.Success },
    var startAction: (ReadWriteSpan, Context) -> Unit = { _, _ -> },
    var endingAction: (ReadWriteSpan) -> Unit = {},
    var endAction: (ReadableSpan) -> Unit = {}
) : SpanProcessor {

    val startCalls = mutableListOf<ReadWriteSpan>()
    val endingCalls = mutableListOf<ReadWriteSpan>()
    val endCalls = mutableListOf<ReadableSpan>()

    override fun onStart(
        span: ReadWriteSpan,
        parentContext: Context
    ) {
        startCalls.add(span)
        startAction(span, parentContext)
    }

    override fun onEnding(span: ReadWriteSpan) {
        endingCalls.add(span)
        endingAction(span)
    }

    override fun onEnd(span: ReadableSpan) {
        endCalls.add(span)
        endAction(span)
    }

    override fun isStartRequired(): Boolean = startRequired
    override fun isEndRequired(): Boolean = endRequired
    override fun forceFlush(): OperationResultCode = flushCode()
    override fun shutdown(): OperationResultCode = shutdownCode()
}
