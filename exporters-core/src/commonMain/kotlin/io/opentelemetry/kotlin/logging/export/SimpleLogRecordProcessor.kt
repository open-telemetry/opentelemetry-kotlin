package io.opentelemetry.kotlin.logging.export

import io.opentelemetry.kotlin.InstrumentationScopeInfo
import io.opentelemetry.kotlin.ReentrantReadWriteLock
import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.error.SdkError
import io.opentelemetry.kotlin.error.SdkErrorHandler
import io.opentelemetry.kotlin.error.SdkErrorSeverity
import io.opentelemetry.kotlin.error.reportError
import io.opentelemetry.kotlin.export.BatchTelemetryDefaults
import io.opentelemetry.kotlin.export.MutableShutdownState
import io.opentelemetry.kotlin.export.OperationResultCode
import io.opentelemetry.kotlin.export.guardExporterCode
import io.opentelemetry.kotlin.export.runWithTimeout
import io.opentelemetry.kotlin.logging.SeverityNumber
import io.opentelemetry.kotlin.logging.data.LogRecordData
import io.opentelemetry.kotlin.logging.model.ReadWriteLogRecord
import kotlin.coroutines.coroutineContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout

/**
 * A simple log record processor that immediately exports log records to a [LogRecordExporter].
 *
 * https://opentelemetry.io/docs/specs/otel/logs/sdk/#built-in-processors
 */
internal class SimpleLogRecordProcessor(
    private val exporter: LogRecordExporter,
    private val scope: CoroutineScope,
    private val sdkErrorHandler: SdkErrorHandler,
) : LogRecordProcessor {

    private val exportMutex = Mutex()
    private val shutdownState = MutableShutdownState()

    // Tracks launched export jobs so forceFlush/shutdown can wait for them. The lock is
    // needed because onEmit() is non-suspend and must register synchronously.
    private val pendingExports = mutableSetOf<Job>()
    private val pendingExportsLock = ReentrantReadWriteLock()

    override fun onEmit(
        log: ReadWriteLogRecord,
        context: Context
    ) {
        admitExport(log)?.start()
    }

    /** Atomically admits [log] under [pendingExportsLock]; the job starts after release. */
    private fun admitExport(log: ReadWriteLogRecord): Job? = pendingExportsLock.write {
        if (shutdownState.isShutdown) {
            null
        } else {
            // Snapshot the log record data before launching the async export, because the
            // live record may be mutated after onEmit returns.
            val data = log.toLogRecordData()
            scope.launch(start = CoroutineStart.LAZY) {
                exportMutex.withLock { exportLogRecord(data) }
            }.also { job ->
                pendingExports.add(job)
                job.invokeOnCompletion {
                    pendingExportsLock.write { pendingExports.remove(job) }
                }
            }
        }
    }

    private suspend fun exportLogRecord(data: LogRecordData) {
        sdkErrorHandler.guardExporterCode("Export failed") {
            try {
                val result = withTimeout(BatchTelemetryDefaults.EXPORT_TIMEOUT_MS) {
                    exporter.export(listOf(data))
                }
                if (result == OperationResultCode.Failure) {
                    sdkErrorHandler.reportError(
                        SdkError.UserCodeError(
                            LogExportUnsuccessfulException(),
                            "Export failed",
                            SdkErrorSeverity.WARNING,
                        )
                    )
                }
                result
            } catch (e: TimeoutCancellationException) {
                sdkErrorHandler.reportError(
                    SdkError.UserCodeError(e, "Export timed out", SdkErrorSeverity.WARNING)
                )
                OperationResultCode.Failure
            }
        }
    }

    override fun enabled(
        context: Context,
        instrumentationScopeInfo: InstrumentationScopeInfo,
        severityNumber: SeverityNumber?,
        eventName: String?,
    ): Boolean = !shutdownState.isShutdown

    override suspend fun forceFlush(): OperationResultCode {
        if (shutdownState.isShutdown) {
            return OperationResultCode.Success
        }
        return runWithTimeout(BatchTelemetryDefaults.FORCE_FLUSH_TIMEOUT_MS) {
            snapshotPendingExports().forEach { it.join() }
            boundedExporterCall("Exporter forceFlush failed") { exporter.forceFlush() }
        }
    }

    override suspend fun shutdown(): OperationResultCode {
        var ownsShutdown = false
        var completed: OperationResultCode? = null
        try {
            // Phase 1: atomically transition to shutdown, then drain pending exports within
            // the shutdown timeout. The transition happens before the action runs, so any
            // export admitted before the snapshot is tracked and any later onEmit is rejected.
            val drainResult = shutdownState.shutdown(BatchTelemetryDefaults.SHUTDOWN_TIMEOUT_MS) {
                ownsShutdown = true
                snapshotPendingExports().forEach { it.join() }
                OperationResultCode.Success
            }
            completed = drainResult
            // Phase 2 runs even when draining timed out or failed, so the exporter is always
            // released. Losers of the shutdown race return Success without cleaning up again.
            return if (ownsShutdown) {
                // Run cleanup in NonCancellable so caller cancellation cannot interrupt
                // exporter shutdown, then re-enter the caller context so cancellation
                // still propagates to the caller afterwards.
                val callerContext = coroutineContext
                val cleanupResult = withContext(NonCancellable) { cleanupExporter() }
                withContext(callerContext) { drainResult.combine(cleanupResult) }
            } else {
                drainResult
            }
        } finally {
            if (ownsShutdown && completed == null) {
                // Shutdown was interrupted before cleanup ran (e.g. caller cancellation).
                // Still release the exporter, bounded, without swallowing the cancellation.
                withContext(NonCancellable) { cleanupExporter() }
            }
        }
    }

    /** Cancels stragglers, then flushes and shuts down the exporter in separate bounded phases. */
    private suspend fun cleanupExporter(): OperationResultCode {
        snapshotPendingExports().forEach { it.cancel() }
        val flushResult = boundedExporterCall("Exporter forceFlush failed") { exporter.forceFlush() }
        val shutdownResult = boundedExporterCall("Exporter shutdown failed") { exporter.shutdown() }
        return flushResult.combine(shutdownResult)
    }

    private suspend fun boundedExporterCall(
        message: String,
        call: suspend () -> OperationResultCode,
    ): OperationResultCode = runWithTimeout(BatchTelemetryDefaults.FORCE_FLUSH_TIMEOUT_MS) {
        exportMutex.withLock { sdkErrorHandler.guardExporterCode(message, call) }
    }

    private fun OperationResultCode.combine(other: OperationResultCode): OperationResultCode =
        if (this == OperationResultCode.Success && other == OperationResultCode.Success) {
            OperationResultCode.Success
        } else {
            OperationResultCode.Failure
        }

    private fun snapshotPendingExports(): List<Job> =
        pendingExportsLock.read { pendingExports.toList() }
}

private class LogExportUnsuccessfulException :
    Exception("LogRecordExporter.export returned OperationResultCode.Failure")
