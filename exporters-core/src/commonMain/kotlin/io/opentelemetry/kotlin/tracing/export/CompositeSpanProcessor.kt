package io.opentelemetry.kotlin.tracing.export

import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.error.SdkErrorHandler
import io.opentelemetry.kotlin.error.guardOrDefault
import io.opentelemetry.kotlin.export.CompositeTelemetryCloseable
import io.opentelemetry.kotlin.export.OperationResultCode
import io.opentelemetry.kotlin.export.TelemetryCloseable
import io.opentelemetry.kotlin.export.batchExportOperation
import io.opentelemetry.kotlin.tracing.model.ReadWriteSpan
import io.opentelemetry.kotlin.tracing.model.ReadableSpan

internal class CompositeSpanProcessor(
    private val processors: List<SpanProcessor>,
    private val sdkErrorHandler: SdkErrorHandler,
    private val telemetryCloseable: TelemetryCloseable = CompositeTelemetryCloseable(
        processors,
        sdkErrorHandler
    ),
) : SpanProcessor, TelemetryCloseable by telemetryCloseable {

    override fun onStart(
        span: ReadWriteSpan,
        parentContext: Context
    ) {
        batchExportOperation(
            processors,
            sdkErrorHandler
        ) {
            if (it.requires(START_DETAILS, SpanProcessor::isStartRequired)) {
                it.onStart(span, parentContext)
            }
            OperationResultCode.Success
        }
    }

    override fun onEnding(span: ReadWriteSpan) {
        batchExportOperation(
            processors,
            sdkErrorHandler
        ) {
            if (it.requires(ON_ENDING_DETAILS, SpanProcessor::isOnEndingRequired)) {
                it.onEnding(span)
            }
            OperationResultCode.Success
        }
    }

    override fun onEnd(span: ReadableSpan) {
        batchExportOperation(
            processors,
            sdkErrorHandler
        ) {
            if (it.requires(END_DETAILS, SpanProcessor::isEndRequired)) {
                it.onEnd(span)
            }
            OperationResultCode.Success
        }
    }

    override fun isStartRequired(): Boolean =
        anyRequires(START_DETAILS, SpanProcessor::isStartRequired)

    override fun isEndRequired(): Boolean =
        anyRequires(END_DETAILS, SpanProcessor::isEndRequired)

    override fun isOnEndingRequired(): Boolean =
        anyRequires(ON_ENDING_DETAILS, SpanProcessor::isOnEndingRequired)

    private fun anyRequires(details: String, predicate: (SpanProcessor) -> Boolean): Boolean {
        return processors.any { it.requires(details, predicate) }
    }

    private fun SpanProcessor.requires(
        details: String,
        predicate: (SpanProcessor) -> Boolean,
    ): Boolean {
        return sdkErrorHandler.guardOrDefault(true, details) { predicate(this) }
    }

    private companion object {
        const val START_DETAILS = "SpanProcessor.isStartRequired failed"
        const val END_DETAILS = "SpanProcessor.isEndRequired failed"
        const val ON_ENDING_DETAILS = "SpanProcessor.isOnEndingRequired failed"
    }
}
