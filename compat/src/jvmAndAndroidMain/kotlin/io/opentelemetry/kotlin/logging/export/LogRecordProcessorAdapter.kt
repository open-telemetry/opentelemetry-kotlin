package io.opentelemetry.kotlin.logging.export

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaLogRecordProcessor
import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.context.toOtelJavaContext
import io.opentelemetry.kotlin.export.OperationResultCode
import io.opentelemetry.kotlin.logging.model.ReadWriteLogRecord
import io.opentelemetry.kotlin.toOperationResultCode

@OptIn(ExperimentalApi::class)
internal class LogRecordProcessorAdapter(
    private val impl: OtelJavaLogRecordProcessor
) : LogRecordProcessor {

    override fun onEmit(
        log: ReadWriteLogRecord,
        context: Context
    ) {
        if (log is ReadWriteLogRecordAdapter) {
            impl.onEmit(context.toOtelJavaContext(), log.impl)
        }
    }

    override suspend fun shutdown(): OperationResultCode = impl.shutdown().toOperationResultCode()
    override suspend fun forceFlush(): OperationResultCode = impl.forceFlush().toOperationResultCode()
}
