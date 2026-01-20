package io.opentelemetry.kotlin.logging.export

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.ReentrantReadWriteLock
import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.export.OperationResultCode
import io.opentelemetry.kotlin.logging.model.ReadWriteLogRecord

/**
 * A simple log record processor that immediately exports log records to a [LogRecordExporter].
 *
 * https://opentelemetry.io/docs/specs/otel/logs/sdk/#built-in-processors
 */
@OptIn(ExperimentalApi::class)
internal class SimpleLogRecordProcessor(
    private val exporter: LogRecordExporter,
) : LogRecordProcessor {

    private val lock = ReentrantReadWriteLock()

    override fun onEmit(
        log: ReadWriteLogRecord,
        context: Context
    ) {
        lock.write {
            exporter.export(listOf(log))
        }
    }

    override suspend fun forceFlush(): OperationResultCode = OperationResultCode.Success
    override suspend fun shutdown(): OperationResultCode = OperationResultCode.Success
}
