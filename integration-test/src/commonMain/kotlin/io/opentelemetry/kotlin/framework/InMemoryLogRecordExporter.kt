package io.opentelemetry.kotlin.framework

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.export.OperationResultCode
import io.opentelemetry.kotlin.logging.export.LogRecordExporter
import io.opentelemetry.kotlin.logging.model.ReadableLogRecord
import kotlin.collections.plusAssign

@OptIn(ExperimentalApi::class)
internal class InMemoryLogRecordExporter : LogRecordExporter {

    private val impl = mutableListOf<ReadableLogRecord>()

    val exportedLogRecords: List<ReadableLogRecord>
        get() = impl

    override fun export(telemetry: List<ReadableLogRecord>): OperationResultCode {
        impl += telemetry
        return OperationResultCode.Success
    }

    override suspend fun shutdown(): OperationResultCode = OperationResultCode.Success
    override suspend fun forceFlush(): OperationResultCode = OperationResultCode.Success
}
