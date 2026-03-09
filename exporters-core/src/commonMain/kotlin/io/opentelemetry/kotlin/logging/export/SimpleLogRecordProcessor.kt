package io.opentelemetry.kotlin.logging.export

import io.opentelemetry.kotlin.InstrumentationScopeInfo
import io.opentelemetry.kotlin.ReentrantReadWriteLock
import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.export.MutableShutdownState
import io.opentelemetry.kotlin.export.OperationResultCode
import io.opentelemetry.kotlin.logging.model.ReadWriteLogRecord
import io.opentelemetry.kotlin.logging.model.SeverityNumber
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * A simple log record processor that immediately exports log records to a [LogRecordExporter].
 *
 * https://opentelemetry.io/docs/specs/otel/logs/sdk/#built-in-processors
 */
internal class SimpleLogRecordProcessor(
    private val exporter: LogRecordExporter,
    private val scope: CoroutineScope,
) : LogRecordProcessor {

    private val lock = ReentrantReadWriteLock()
    private val shutdownState = MutableShutdownState()

    override fun onEmit(
        log: ReadWriteLogRecord,
        context: Context
    ) {
        shutdownState.execute {
            scope.launch {
                lock.write {
                    exporter.export(listOf(log))
                }
            }
        }
    }

    override fun enabled(
        context: Context,
        instrumentationScopeInfo: InstrumentationScopeInfo,
        severityNumber: SeverityNumber?,
        eventName: String?,
    ): Boolean = !shutdownState.isShutdown

    override suspend fun forceFlush(): OperationResultCode = exporter.forceFlush()

    override suspend fun shutdown(): OperationResultCode =
        shutdownState.shutdown {
            exporter.shutdown()
        }
}
