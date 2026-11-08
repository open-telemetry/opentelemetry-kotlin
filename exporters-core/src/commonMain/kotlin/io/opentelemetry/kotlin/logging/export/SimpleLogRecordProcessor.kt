package io.opentelemetry.kotlin.logging.export

import io.opentelemetry.kotlin.InstrumentationScopeInfo
import io.opentelemetry.kotlin.ReentrantReadWriteLock
import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.error.SdkErrorHandler
import io.opentelemetry.kotlin.error.SdkErrorSeverity
import io.opentelemetry.kotlin.error.SdkError
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
import kotlin.coroutines.coroutineContext

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

    // Tracks every launched export job and its result.
    // A ReentrantReadWriteLock is used because onEmit() is non-suspend and must register
    // jobs synchronously, while forceFlush/shutdown (suspend) need a consistent snapshot.
    private val pendingExports = mutableMapOf<Job, ExportResult>()
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
                val result = try {
                    val resultCode = exportMutex.withLock {
                        sdkErrorHandler.guardExporterCode("Export failed") {
                            runWithTimeout(BatchTelemetryDefaults.EXPORT_TIMEOUT_MS) {
                                exporter.export(listOf(data))
                            }
                        }
                    }
                    if (resultCode == OperationResultCode.Failure) {
                        ExportResult.Failed
                    } else {
                        ExportResult.Success
                    }
                } catch (e: Throwable) {
                    // Exceptions are already reported by guardExporterCode
                    ExportResult.Threw(e)
                }
                markExportComplete(coroutineContext[Job]!!, result)
            }
            // Register job immediately - the job is already launched, so we track it
            pendingExportsLock.write { pendingExports[job] = ExportResult.Pending }
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
            val exportResults = drainPendingExports()
            // Report any export failures that occurred during the flush window
            reportExportFailures(exportResults)
            sdkErrorHandler.guardExporterCode("Exporter forceFlush failed") {
                exporter.forceFlush()
            }
        }
    }

    override suspend fun shutdown(): OperationResultCode =
        shutdownState.shutdown(BatchTelemetryDefaults.SHUTDOWN_TIMEOUT_MS) {
            // Drain pending exports
            val exportResults = drainPendingExports()
            // Report any export failures that occurred before shutdown
            reportExportFailures(exportResults)

            // Ensure exporter cleanup happens even if timeout elapses
            try {
                sdkErrorHandler.guardExporterCode("Exporter forceFlush failed") {
                    exporter.forceFlush()
                } and sdkErrorHandler.guardExporterCode("Exporter shutdown failed") {
                    exporter.shutdown()
                }
            } catch (e: Throwable) {
                // Exceptions are already reported by guardExporterCode
                OperationResultCode.Failure
            }
        }

    /**
     * Marks an export job as complete with its result, removing it from the tracking map.
     * This is called from the job's completion handler, so it can be called without locks.
     */
    private fun markExportComplete(job: Job, result: ExportResult) {
        pendingExportsLock.write { pendingExports.remove(job) }
    }

    /**
     * Waits for all currently pending exports to complete and returns their results.
     */
    private suspend fun drainPendingExports(): List<ExportResult> {
        val jobs = pendingExportsLock.read { pendingExports.keys.toList() }
        jobs.forEach { it.join() }
        return pendingExportsLock.read { pendingExports.values.toList() }
    }

    /**
     * Reports export failures (either thrown exceptions or returned Failure) to SdkErrorHandler.
     * Timeouts are reported when runWithTimeout returns Failure.
     */
    private fun reportExportFailures(results: List<ExportResult>) {
        results.forEach { result ->
            when (result) {
                is ExportResult.Success -> {
                    // No error to report
                }
                is ExportResult.Failed -> {
                    // Export returned Failure - report as SDK code error
                    // Use reportError directly since we don't have access to reportSdkCodeError helper
                    sdkErrorHandler.onError(
                        SdkError.SdkCodeError(
                            RuntimeException("Export returned OperationResultCode.Failure"),
                            "Export failed",
                            SdkErrorSeverity.WARNING
                        )
                    )
                }
                is ExportResult.Threw -> {
                    // Exceptions are already reported by guardExporterCode in the export job
                    // No additional reporting needed
                }
                is ExportResult.Pending -> {
                    // Should not happen after join(), but handle defensively
                    sdkErrorHandler.onError(
                        SdkError.SdkCodeError(
                            RuntimeException("Export marked as Pending after completion"),
                            "Export failed",
                            SdkErrorSeverity.WARNING
                        )
                    )
                }
            }
        }
    }

    /**
     * Sealed class representing the completion state of an export job.
     */
    private sealed class ExportResult {
        data object Success : ExportResult()
        data object Failed : ExportResult()
        data class Threw(val exception: Throwable) : ExportResult()
        data object Pending : ExportResult()
    }
}
