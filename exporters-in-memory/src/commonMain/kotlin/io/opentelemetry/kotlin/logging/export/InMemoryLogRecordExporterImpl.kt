package io.opentelemetry.kotlin.logging.export

import io.opentelemetry.kotlin.export.MutableShutdownState
import io.opentelemetry.kotlin.export.OperationResultCode
import io.opentelemetry.kotlin.logging.data.LogRecordData
import io.opentelemetry.kotlin.threadSafeList

internal class InMemoryLogRecordExporterImpl : InMemoryLogRecordExporter {

    private val impl = threadSafeList<LogRecordData>()
    private val shutdownState = MutableShutdownState()

    override val exportedLogRecords: List<LogRecordData>
        get() = impl.toList()

    override suspend fun export(telemetry: List<LogRecordData>): OperationResultCode =
        shutdownState.ifActive {
            impl += telemetry
            OperationResultCode.Success
        }

    override suspend fun forceFlush(): OperationResultCode = OperationResultCode.Success

    override suspend fun shutdown(): OperationResultCode =
        shutdownState.shutdown {
            OperationResultCode.Success
        }
}
