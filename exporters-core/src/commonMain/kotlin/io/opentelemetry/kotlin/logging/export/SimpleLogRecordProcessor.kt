package io.opentelemetry.kotlin.logging.export

import io.opentelemetry.kotlin.InstrumentationScopeInfo
import io.opentelemetry.kotlin.ReentrantReadWriteLock
import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.error.SdkErrorHandler
import io.opentelemetry.kotlin.export.BatchTelemetryDefaults
import io.opentelemetry.kotlin.export.MutableShutdownState
import io.opentelemetry.kotlin.export.OperationResultCode
import io.opentelemetry.kotlin.export.and
import io.opentelemetry.kotlin.export.guardExporterCode
import io.opentelemetry.kotlin.export.runWithTimeout
import io.opentelemetry.kotlin.logging.SeverityNumber
import io.opentelemetry.kotlin.logging.model.ReadWriteLogRecord
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

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

    // Tracks every launched export job so that forceFlush/shutdown can wait for them.
    // A ReentrantReadWriteLock is used because onEmit() is non-suspend and must register
    // jobs synchronously, while forceFlush/shutdown (suspend) need a consistent snapshot.
    private val pendingExports = mutableSetOf<Job>()
    private val pendingExportsLock = ReentrantReadWriteLock()

    override fun onEmit(
        log: ReadWriteLogRecord,
        context: Context
    ) {
        shutdownState.execute {
            // Snapshot the log record data before launching the async export, because the
            // live record may be mutated after onEmit returns.
            val data = log.toLogRecordData()
            val job = scope.launch {
                exportMutex.withLock {
                    sdkErrorHandler.guardExporterCode("Export failed") {
                        runWithTimeout(BatchTelemetryDefaults.EXPORT_TIMEOUT_MS) {
                            exporter.export(listOf(data))
                        }
                    }
                }
            }
            registerJob(job)
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
            sdkErrorHandler.guardExporterCode("Exporter forceFlush failed") {
                exporter.forceFlush()
            }
        }
    }

    override suspend fun shutdown(): OperationResultCode =
        shutdownState.shutdown(BatchTelemetryDefaults.SHUTDOWN_TIMEOUT_MS) {
            snapshotPendingExports().forEach { it.join() }
            sdkErrorHandler.guardExporterCode("Exporter forceFlush failed") {
                exporter.forceFlush()
            } and sdkErrorHandler.guardExporterCode("Exporter shutdown failed") {
                exporter.shutdown()
            }
        }

    /**
     * Adds [job] to the tracking set and arranges for its removal once it completes, so the
     * set never grows unbounded. Both operations use the write lock because they are called
     * from the non-suspend [onEmit] path.
     */
    private fun registerJob(job: Job) {
        pendingExportsLock.write { pendingExports.add(job) }
        job.invokeOnCompletion { pendingExportsLock.write { pendingExports.remove(job) } }
    }

    /**
     * Returns a point-in-time copy of the tracked export jobs. The read lock guarantees the
     * snapshot is consistent with any job that was registered before this call.
     */
    private fun snapshotPendingExports(): List<Job> =
        pendingExportsLock.read { pendingExports.toList() }
}
