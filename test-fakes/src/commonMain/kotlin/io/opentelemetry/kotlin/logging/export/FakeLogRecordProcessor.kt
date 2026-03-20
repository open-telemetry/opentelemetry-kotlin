package io.opentelemetry.kotlin.logging.export

import io.opentelemetry.kotlin.InstrumentationScopeInfo
import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.export.OperationResultCode
import io.opentelemetry.kotlin.logging.SeverityNumber
import io.opentelemetry.kotlin.logging.model.ReadWriteLogRecord

class FakeLogRecordProcessor(
    var flushCode: () -> OperationResultCode = { OperationResultCode.Success },
    var shutdownCode: () -> OperationResultCode = { OperationResultCode.Success },
    var action: (log: ReadWriteLogRecord, context: Context) -> Unit = { _, _ -> },
    var enabledResult: () -> Boolean = { true },
) : LogRecordProcessor {

    val logs: MutableList<ReadWriteLogRecord> = mutableListOf()

    override fun onEmit(
        log: ReadWriteLogRecord,
        context: Context
    ) {
        logs.add(log)
        action(log, context)
    }

    override fun enabled(
        context: Context,
        instrumentationScopeInfo: InstrumentationScopeInfo,
        severityNumber: SeverityNumber?,
        eventName: String?,
    ): Boolean = enabledResult()

    override suspend fun forceFlush(): OperationResultCode = flushCode()
    override suspend fun shutdown(): OperationResultCode = shutdownCode()
}
