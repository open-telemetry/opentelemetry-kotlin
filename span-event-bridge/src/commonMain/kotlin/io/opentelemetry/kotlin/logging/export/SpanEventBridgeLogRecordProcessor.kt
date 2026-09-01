package io.opentelemetry.kotlin.logging.export

import io.opentelemetry.kotlin.InstrumentationScopeInfo
import io.opentelemetry.kotlin.attributes.setAttributes
import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.export.MutableShutdownState
import io.opentelemetry.kotlin.export.OperationResultCode
import io.opentelemetry.kotlin.logging.SeverityNumber
import io.opentelemetry.kotlin.logging.model.ReadWriteLogRecord
import io.opentelemetry.kotlin.tracing.Span
import io.opentelemetry.kotlin.tracing.SpanContext

/**
 * A log record processor that copies event log records onto the current span as a span event.
 *
 * https://opentelemetry.io/docs/specs/otel/logs/sdk/#event-to-span-event-bridge
 */
internal class SpanEventBridgeLogRecordProcessor : LogRecordProcessor {

    private val shutdownState = MutableShutdownState()

    override fun onEmit(log: ReadWriteLogRecord, context: Context) {
        shutdownState.execute {
            bridgeToSpanEvent(log, context)
        }
    }

    override fun enabled(
        context: Context,
        instrumentationScopeInfo: InstrumentationScopeInfo,
        severityNumber: SeverityNumber?,
        eventName: String?,
    ): Boolean = !shutdownState.isShutdown &&
        !eventName.isNullOrEmpty() &&
        context.extractSpan().isRecording()

    override suspend fun forceFlush(): OperationResultCode = OperationResultCode.Success

    override suspend fun shutdown(): OperationResultCode =
        shutdownState.shutdown { OperationResultCode.Success }

    private fun bridgeToSpanEvent(log: ReadWriteLogRecord, context: Context) {
        val eventName = log.eventName
        if (eventName.isNullOrEmpty()) {
            return
        }
        val span = context.extractSpan()
        if (!span.isRecording() || !log.spanContext.identifies(span)) {
            return
        }
        val attributes = log.attributes
        span.addEvent(eventName, log.timestamp ?: log.observedTimestamp) {
            setAttributes(attributes)
        }
    }

    private fun SpanContext.identifies(span: Span): Boolean = isValid &&
        traceIdBytes.contentEquals(span.spanContext.traceIdBytes) &&
        spanIdBytes.contentEquals(span.spanContext.spanIdBytes)
}
