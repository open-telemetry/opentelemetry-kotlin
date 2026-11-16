package io.opentelemetry.kotlin.logging.export

import io.opentelemetry.kotlin.error.SdkErrorHandler
import io.opentelemetry.kotlin.error.guardOrDefault
import io.opentelemetry.kotlin.export.MutableShutdownState
import io.opentelemetry.kotlin.export.OperationResultCode
import io.opentelemetry.kotlin.logging.data.LogRecordData
import io.opentelemetry.kotlin.platformLog

/**
 * A [LogRecordExporter] that outputs log records to stdout.
 */
internal class StdoutLogRecordExporter(
    private val sdkErrorHandler: SdkErrorHandler,
    private val logger: (String) -> Unit = ::platformLog,
) : LogRecordExporter {

    private val shutdownState = MutableShutdownState()

    override suspend fun export(telemetry: List<LogRecordData>): OperationResultCode =
        shutdownState.ifActive {
            val failures = telemetry.count { logRecord ->
                !sdkErrorHandler.guardOrDefault(false, "Failed to write log record to stdout") {
                    logger(formatLogRecord(logRecord))
                    true
                }
            }
            if (failures == 0) {
                OperationResultCode.Success
            } else {
                OperationResultCode.Failure
            }
        }

    override suspend fun forceFlush(): OperationResultCode = OperationResultCode.Success

    override suspend fun shutdown(): OperationResultCode =
        shutdownState.shutdown {
            OperationResultCode.Success
        }

    private fun formatLogRecord(logRecord: LogRecordData): String = buildString {
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
            appendLine(formatBody(it))
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

    private fun formatBody(body: Any): String = try {
        body.toString()
    } catch (ignored: Throwable) {
        "<unavailable>"
    }
}
