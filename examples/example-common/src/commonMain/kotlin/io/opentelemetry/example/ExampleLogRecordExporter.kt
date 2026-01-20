package io.opentelemetry.example

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.export.OperationResultCode
import io.opentelemetry.kotlin.logging.export.LogRecordExporter
import io.opentelemetry.kotlin.logging.model.ReadableLogRecord

@OptIn(ExperimentalApi::class)
internal class ExampleLogRecordExporter : LogRecordExporter {

    override fun export(telemetry: List<ReadableLogRecord>): OperationResultCode {
        telemetry.forEach {
            println("Exporting log: $it")
        }
        return OperationResultCode.Success
    }

    override suspend fun forceFlush(): OperationResultCode = OperationResultCode.Success
    override suspend fun shutdown(): OperationResultCode = OperationResultCode.Success
}
