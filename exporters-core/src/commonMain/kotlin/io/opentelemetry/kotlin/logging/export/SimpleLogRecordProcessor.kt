package io.opentelemetry.kotlin.logging.export

import io.opentelemetry.kotlin.InstrumentationScopeInfo
import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.error.SdkError
import io.opentelemetry.kotlin.error.SdkErrorHandler
import io.opentelemetry.kotlin.error.SdkErrorSeverity
import io.opentelemetry.kotlin.error.reportError
import io.opentelemetry.kotlin.export.BatchTelemetryDefaults
import io.opentelemetry.kotlin.export.ExportTracker
import io.opentelemetry.kotlin.export.MutableShutdownState
import io.opentelemetry.kotlin.export.OperationResultCode
import io.opentelemetry.kotlin.export.and
import io.opentelemetry.kotlin.export.guardExporterCode
import io.opentelemetry.kotlin.export.runWithTimeout
import io.opentelemetry.kotlin.logging.SeverityNumber
import io.opentelemetry.kotlin.logging.data.LogRecordData
import io.opentelemetry.kotlin.logging.model.ReadWriteLogRecord
import kotlin.coroutines.coroutineContext
import kotlin.time.TimeSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.ensureActive
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

    // Serializes exporter.export calls; the spec only forbids concurrent Export calls, so
    // forceFlush/shutdown deliberately do not take this mutex.
    private val exportMutex = Mutex()
    private val shutdownState = MutableShutdownState()
    private val exportTracker = ExportTracker(scope.coroutineContext)

    override fun onEmit(
        log: ReadWriteLogRecord,
        context: Context
    ) {
        // Snapshot the log record data before launching the async export, because the
        // live record may be mutated after onEmit returns.
        val data = log.toLogRecordData()
        exportTracker.launch {
            exportMutex.withLock { exportLogRecord(data) }
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
            exportTracker.drain()
            sdkErrorHandler.guardExporterCode("Exporter forceFlush failed") {
                exporter.forceFlush()
            }
        }
    }

    override suspend fun shutdown(): OperationResultCode {
        // Stop admissions before the shutdown transition so an export racing shutdown is
        // either already tracked as a child job or rejected outright.
        exportTracker.close()
        return shutdownState.shutdown(BatchTelemetryDefaults.SHUTDOWN_TIMEOUT_MS) {
            shutdownWithinDeadline()
        }
    }

    /**
     * Runs drain, exporter forceFlush and exporter shutdown against a single overall deadline.
     * Each phase is bounded by the time remaining in [BatchTelemetryDefaults.SHUTDOWN_TIMEOUT_MS],
     * so the phases cannot each consume a fresh timeout. Cleanup runs in [NonCancellable] so
     * caller cancellation cannot skip exporter shutdown, and [ensureActive] re-propagates that
     * cancellation once cleanup has finished or the deadline has expired.
     */
    private suspend fun shutdownWithinDeadline(): OperationResultCode {
        val start = TimeSource.Monotonic.markNow()
        fun remainingMs(): Long =
            (BatchTelemetryDefaults.SHUTDOWN_TIMEOUT_MS - start.elapsedNow().inWholeMilliseconds)
                .coerceAtLeast(1)

        var result = withContext(NonCancellable) {
            runWithTimeout(remainingMs()) {
                exportTracker.drain()
                OperationResultCode.Success
            }
        }
        exportTracker.cancelRemaining()
        result = result and withContext(NonCancellable) {
            sdkErrorHandler.guardExporterCode("Exporter forceFlush failed") {
                runWithTimeout(remainingMs()) { exporter.forceFlush() }
            }
        }
        result = result and withContext(NonCancellable) {
            sdkErrorHandler.guardExporterCode("Exporter shutdown failed") {
                runWithTimeout(remainingMs()) { exporter.shutdown() }
            }
        }
        coroutineContext.ensureActive()
        return result
    }
}

private class LogExportUnsuccessfulException :
    Exception("LogRecordExporter.export returned OperationResultCode.Failure")
