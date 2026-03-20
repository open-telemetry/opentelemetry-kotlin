package io.opentelemetry.kotlin.logging.export

import io.opentelemetry.kotlin.InstrumentationScopeInfo
import io.opentelemetry.kotlin.aliases.OtelJavaLogRecordProcessor
import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.context.toOtelJavaContext
import io.opentelemetry.kotlin.export.MutableShutdownState
import io.opentelemetry.kotlin.export.OperationResultCode
import io.opentelemetry.kotlin.logging.SeverityNumber
import io.opentelemetry.kotlin.logging.model.ReadWriteLogRecord
import io.opentelemetry.kotlin.toOperationResultCode

internal class LogRecordProcessorAdapter(
    private val impl: OtelJavaLogRecordProcessor
) : LogRecordProcessor {

    private val shutdownState = MutableShutdownState()

    override fun onEmit(
        log: ReadWriteLogRecord,
        context: Context
    ) {
        shutdownState.execute {
            if (log is ReadWriteLogRecordAdapter) {
                impl.onEmit(context.toOtelJavaContext(), log.impl)
            }
        }
    }

    override fun enabled(
        context: Context,
        instrumentationScopeInfo: InstrumentationScopeInfo,
        severityNumber: SeverityNumber?,
        eventName: String?,
    ): Boolean = !shutdownState.isShutdown

    override suspend fun forceFlush(): OperationResultCode = impl.forceFlush().toOperationResultCode()

    override suspend fun shutdown(): OperationResultCode =
        shutdownState.shutdown {
            impl.shutdown().toOperationResultCode()
        }
}
