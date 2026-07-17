package io.opentelemetry.kotlin.file.export

import io.opentelemetry.kotlin.export.MutableShutdownState
import io.opentelemetry.kotlin.export.OperationResultCode
import io.opentelemetry.kotlin.logging.export.LogRecordExporter
import io.opentelemetry.kotlin.logging.model.ReadableLogRecord
import io.opentelemetry.kotlin.tracing.data.SpanData
import io.opentelemetry.kotlin.tracing.export.SpanExporter
import okio.BufferedSink

/**
 * A [LogRecordExporter] that outputs log records to a file.
 */
internal class FileLogRecordExporter(
    private val sink: BufferedSink,
    private val encoder: JsonLogRecordExporter
) : LogRecordExporter {
    private val shutdownState = MutableShutdownState()

    override suspend fun export(telemetry: List<ReadableLogRecord>): OperationResultCode =
        shutdownState.ifActive {
            telemetry.forEach { logRecord ->
                sink.writeUtf8(encoder.encode(logRecord))
                sink.writeUtf8("\n")
            }
            sink.flush()
            OperationResultCode.Success
        }

    override suspend fun forceFlush(): OperationResultCode = OperationResultCode.Success

    override suspend fun shutdown(): OperationResultCode =
        shutdownState.shutdown {
            OperationResultCode.Success
        }
}

/**
 * A [SpanExporter] that outputs log records to a file.
 */
internal class FileSpanExporter(
    private val sink: BufferedSink,
    private val encoder: OtlpJsonSpanEncoder
) : SpanExporter {
    private val shutdownState = MutableShutdownState()

    override suspend fun export(telemetry: List<SpanData>): OperationResultCode =
        shutdownState.ifActive {
            telemetry.forEach { spanData ->
                sink.writeUtf8(encoder.encode(spanData))
                sink.writeUtf8("\n")
            }
            sink.flush()
            OperationResultCode.Success
        }

    override suspend fun forceFlush(): OperationResultCode = OperationResultCode.Success

    override suspend fun shutdown(): OperationResultCode =
        shutdownState.shutdown {
            OperationResultCode.Success
        }
}
