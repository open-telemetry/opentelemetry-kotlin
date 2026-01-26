package io.opentelemetry.kotlin.logging.export

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.export.OperationResultCode
import io.opentelemetry.kotlin.logging.model.ReadableLogRecord

/**
 * A [LogRecordExporter] that outputs log records to stdout.
 */
@OptIn(ExperimentalApi::class)
internal class StdoutLogRecordExporter(
    private val logger: (String) -> Unit = ::println
) : LogRecordExporter {

    override suspend fun export(telemetry: List<ReadableLogRecord>): OperationResultCode {
        telemetry.forEach { logRecord ->
            logger(formatLogRecord(logRecord))
        }
        return OperationResultCode.Success
    }

    override suspend fun forceFlush(): OperationResultCode = OperationResultCode.Success
    override suspend fun shutdown(): OperationResultCode = OperationResultCode.Success

    private fun formatLogRecord(logRecord: ReadableLogRecord): String = buildString {
        append("LogRecord")
        logRecord.severityNumber?.let {
            append(" [")
            append(it)
            append("]")
        }
        appendLine()

        logRecord.timestamp?.let {
            append("  Timestamp: ")
            appendLine(it)
        }

        logRecord.observedTimestamp?.let {
            append("  ObservedTimestamp: ")
            appendLine(it)
        }

        logRecord.severityText?.let {
            append("  SeverityText: ")
            appendLine(it)
        }

        logRecord.body?.let {
            append("  Body: ")
            appendLine(it)
        }

        logRecord.eventName?.let {
            append("  EventName: ")
            appendLine(it)
        }

        append("  TraceId: ")
        appendLine(logRecord.spanContext.traceId)
        append("  SpanId: ")
        appendLine(logRecord.spanContext.spanId)

        if (logRecord.attributes.isNotEmpty()) {
            appendLine("  Attributes:")
            logRecord.attributes.forEach { (key, value) ->
                append("    ")
                append(key)
                append(": ")
                appendLine(value)
            }
        }

        append("  Resource: ")
        appendLine(logRecord.resource.attributes)

        append("  InstrumentationScope: ")
        append(logRecord.instrumentationScopeInfo.name)
        logRecord.instrumentationScopeInfo.version?.let {
            append(":")
            append(it)
        }
    }
}
