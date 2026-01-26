package io.opentelemetry.kotlin.logging.export

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.export.OperationResultCode
import io.opentelemetry.kotlin.logging.model.ReadableLogRecord

@OptIn(ExperimentalApi::class)
internal class InMemoryLogRecordExporterImpl : InMemoryLogRecordExporter {

    private val impl = mutableListOf<ReadableLogRecord>()

    override val exportedLogRecords: List<ReadableLogRecord>
        get() = impl

    override suspend fun export(telemetry: List<ReadableLogRecord>): OperationResultCode {
        impl += telemetry
        return OperationResultCode.Success
    }

    override suspend fun shutdown(): OperationResultCode = OperationResultCode.Success
    override suspend fun forceFlush(): OperationResultCode = OperationResultCode.Success
}
