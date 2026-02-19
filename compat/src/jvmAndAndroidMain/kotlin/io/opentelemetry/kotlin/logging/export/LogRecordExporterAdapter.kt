package io.opentelemetry.kotlin.logging.export

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaLogRecordExporter
import io.opentelemetry.kotlin.export.MutableShutdownState
import io.opentelemetry.kotlin.export.OperationResultCode
import io.opentelemetry.kotlin.logging.model.ReadableLogRecord
import io.opentelemetry.kotlin.toOperationResultCode

@OptIn(ExperimentalApi::class)
internal class LogRecordExporterAdapter(
    private val impl: OtelJavaLogRecordExporter
) : LogRecordExporter {

    private val shutdownState = MutableShutdownState()

    override suspend fun export(telemetry: List<ReadableLogRecord>): OperationResultCode =
        shutdownState.ifActive {
            impl.export(telemetry.map(ReadableLogRecord::toLogRecordData)).toOperationResultCode()
        }

    override suspend fun forceFlush(): OperationResultCode = impl.flush().toOperationResultCode()

    override suspend fun shutdown(): OperationResultCode =
        shutdownState.ifActive(OperationResultCode.Success) {
            shutdownState.shutdown()
            impl.shutdown().toOperationResultCode()
        }
}
