package io.opentelemetry.kotlin.logging.export

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.export.OperationResultCode
import io.opentelemetry.kotlin.logging.model.ReadWriteLogRecord

@OptIn(ExperimentalApi::class)
class FakeLogRecordProcessor(
    var flushCode: () -> OperationResultCode = { OperationResultCode.Success },
    var shutdownCode: () -> OperationResultCode = { OperationResultCode.Success },
    var action: (log: ReadWriteLogRecord, context: Context) -> Unit = { _, _ -> }
) : LogRecordProcessor {

    val logs: MutableList<ReadWriteLogRecord> = mutableListOf()

    override fun onEmit(
        log: ReadWriteLogRecord,
        context: Context
    ) {
        logs.add(log)
        action(log, context)
    }

    override fun forceFlush(): OperationResultCode = flushCode()
    override fun shutdown(): OperationResultCode = shutdownCode()
}
