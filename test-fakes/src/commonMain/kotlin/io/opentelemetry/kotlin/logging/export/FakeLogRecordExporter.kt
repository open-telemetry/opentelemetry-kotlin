package io.opentelemetry.kotlin.logging.export

import io.opentelemetry.kotlin.export.OperationResultCode
import io.opentelemetry.kotlin.logging.data.LogRecordData

class FakeLogRecordExporter(
    var flushCode: () -> OperationResultCode = { OperationResultCode.Success },
    var shutdownCode: () -> OperationResultCode = { OperationResultCode.Success },
    var action: (telemetry: List<LogRecordData>) -> OperationResultCode = { OperationResultCode.Success }
) : LogRecordExporter {

    val logs: MutableList<LogRecordData> = mutableListOf()

    override suspend fun export(telemetry: List<LogRecordData>): OperationResultCode {
        logs += telemetry
        return action(telemetry)
    }

    override suspend fun forceFlush(): OperationResultCode = flushCode()
    override suspend fun shutdown(): OperationResultCode = shutdownCode()
}
