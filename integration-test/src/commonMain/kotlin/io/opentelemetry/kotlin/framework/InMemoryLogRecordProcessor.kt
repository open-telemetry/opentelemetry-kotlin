package io.opentelemetry.kotlin.framework

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.export.OperationResultCode
import io.opentelemetry.kotlin.logging.export.LogRecordProcessor
import io.opentelemetry.kotlin.logging.model.ReadWriteLogRecord
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalApi::class)
internal class InMemoryLogRecordProcessor(
    private val exporter: InMemoryLogRecordExporter,
    private val scope: CoroutineScope,
) : LogRecordProcessor {

    override fun onEmit(log: ReadWriteLogRecord, context: Context) {
        scope.launch {
            exporter.export(listOf(log))
        }
    }

    override suspend fun shutdown(): OperationResultCode = OperationResultCode.Success
    override suspend fun forceFlush(): OperationResultCode = OperationResultCode.Success
}
