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
import io.opentelemetry.kotlin.export.and
import io.opentelemetry.kotlin.export.guardExporterCode
import io.opentelemetry.kotlin.export.runWithTimeout
import io.opentelemetry.kotlin.logging.SeverityNumber
import io.opentelemetry.kotlin.logging.data.LogRecordData
import io.opentelemetry.kotlin.logging.model.ReadWriteLogRecord
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlin.coroutines.coroutineContext
import kotlin.time.TimeSource

/**
 * A simple log record processor that immediately exports log records to a [LogRecordExporter].
 *
 * https://opentelemetry.io/docs/specs/otel/logs/sdk/#built-in-processors
 */
internal class SimpleLogRecordProcessor(
    private val exporter: LogRecordExporter,
    private val scope: CoroutineScope,
    private val sdkErrorHandler: SdkErrorHandler,
    private val nowMs: (() -> Long)? = null,
) : LogRecordProcessor {

    private val monotonicMark = TimeSource.Monotonic.markNow()
    private val now: () -> Long = nowMs ?: { monotonicMark.elapsedNow().inWholeMilliseconds }

    // Serializes exporter.export calls; the spec only forbids concurrent Export calls, so
    // forceFlush/shutdown deliberately do not take this mutex.
    private val exportMutex = Mutex()
    private val shutdownState = MutableShutdownState()

    // Tracks admitted export jobs so drain can wait for them and shutdown can reject new
    // admissions. Exports are children of a dedicated SupervisorJob that is deliberately
    // not parented to the scope's Job (see init); admission and closure are serialized by
    // trackerLock so an admitted export can never escape drain's children snapshot.
    internal val trackerJob = SupervisorJob()
    private val trackerScope = CoroutineScope(scope.coroutineContext + trackerJob)
    private val trackerLock = ReentrantReadWriteLock()
    private var trackerClosed = false

    init {
        // Propagate parent completion/cancellation without making trackerJob a child of the
        // parent's Job: a child Job would be reported as an uncompleted child coroutine
        // by runTest-style scopes that never complete until the test ends.
        scope.coroutineContext[Job]?.invokeOnCompletion { trackerJob.cancel() }
    }

    private fun launchExport(block: suspend CoroutineScope.() -> Unit): Boolean = trackerLock.write {
        if (trackerClosed) {
            false
        } else {
            trackerScope.launch(block = block)
            true
        }
    }

    private suspend fun drainExports() {
        trackerJob.children.toList().joinAll()
    }

    private fun closeExports() {
        trackerLock.write { trackerClosed = true }
    }

    override fun onEmit(
        log: ReadWriteLogRecord,
        context: Context
    ) {
        // Snapshot the log record data before launching the async export, because the
        // live record may be mutated after onEmit returns.
        val data = log.toLogRecordData()
        launchExport {
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
                // A timeout raised by our own withTimeout leaves the coroutine active; an
                // externally cancelled export coroutine must propagate instead of being
                // misreported as an exporter failure.
                coroutineContext.ensureActive()
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
            drainExports()
            sdkErrorHandler.guardExporterCode("Exporter forceFlush failed") {
                exporter.forceFlush()
            }
        }
    }

    override suspend fun shutdown(): OperationResultCode {
        // Stop admissions before the shutdown transition so an export racing shutdown is
        // either already tracked as a child job or rejected outright.
        closeExports()
        return shutdownState.shutdown(BatchTelemetryDefaults.SHUTDOWN_TIMEOUT_MS) {
            shutdownWithinDeadline()
        }
    }

    /**
     * Runs drain, exporter forceFlush and exporter shutdown against a single overall deadline:
     * each phase gets only the time remaining in [BatchTelemetryDefaults.SHUTDOWN_TIMEOUT_MS].
     * Phases run in [NonCancellable] so caller cancellation cannot skip exporter cleanup;
     * [ensureActive] re-propagates that cancellation once cleanup finishes or times out.
     */
    private suspend fun shutdownWithinDeadline(): OperationResultCode {
        val start = now()
        fun remainingMs(): Long =
            (BatchTelemetryDefaults.SHUTDOWN_TIMEOUT_MS - (now() - start)).coerceAtLeast(0)

        suspend fun exporterPhase(
            errorMessage: String,
            floorMs: Long = 0,
            block: suspend () -> OperationResultCode,
        ): OperationResultCode = withContext(NonCancellable) {
            sdkErrorHandler.guardExporterCode(errorMessage) {
                runWithTimeout(remainingMs().coerceAtLeast(floorMs)) { block() }
            }
        }

        var result = withContext(NonCancellable) {
            runWithTimeout(remainingMs()) {
                drainExports()
                OperationResultCode.Success
            }
        }
        trackerJob.cancelChildren()
        result = result and exporterPhase("Exporter forceFlush failed") { exporter.forceFlush() }
        // Single 1ms floor so shutdown is still attempted when the deadline is exactly
        // exhausted; drain and forceFlush get no such floor.
        result = result and exporterPhase("Exporter shutdown failed", floorMs = 1) {
            exporter.shutdown()
        }
        // All admitted exports have been drained or cancelled; release the tracker job.
        closeExports()
        trackerJob.cancel()
        coroutineContext.ensureActive()
        return result
    }
}

private class LogExportUnsuccessfulException :
    Exception("LogRecordExporter.export returned OperationResultCode.Failure")
