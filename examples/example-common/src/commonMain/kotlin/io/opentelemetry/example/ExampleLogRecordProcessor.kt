package io.opentelemetry.example

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.export.OperationResultCode
import io.opentelemetry.kotlin.logging.export.LogRecordProcessor
import io.opentelemetry.kotlin.logging.model.ReadWriteLogRecord

@OptIn(ExperimentalApi::class)
internal class ExampleLogRecordProcessor : LogRecordProcessor {

    private val exporter: ExampleLogRecordExporter = ExampleLogRecordExporter()

    override fun onEmit(
        log: ReadWriteLogRecord,
        context: Context
    ) {
        exporter.export(listOf(log))
    }

    override fun forceFlush(): OperationResultCode = OperationResultCode.Success
    override fun shutdown(): OperationResultCode = OperationResultCode.Success
}
