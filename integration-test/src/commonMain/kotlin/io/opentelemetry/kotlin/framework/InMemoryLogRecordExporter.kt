package io.opentelemetry.kotlin.framework

import io.opentelemetry.kotlin.export.OperationResultCode
import io.opentelemetry.kotlin.logging.data.LogRecordData
import io.opentelemetry.kotlin.logging.export.LogRecordExporter
import kotlin.collections.plusAssign

internal class InMemoryLogRecordExporter : LogRecordExporter {

    private val impl = mutableListOf<LogRecordData>()

    val exportedLogRecords: List<LogRecordData>
        get() = impl

    override suspend fun export(telemetry: List<LogRecordData>): OperationResultCode {
        impl += telemetry
        return OperationResultCode.Success
    }

    override suspend fun shutdown(): OperationResultCode = OperationResultCode.Success
    override suspend fun forceFlush(): OperationResultCode = OperationResultCode.Success
}
