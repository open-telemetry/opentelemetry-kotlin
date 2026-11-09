package io.opentelemetry.kotlin.tracing.export

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
import io.opentelemetry.kotlin.tracing.model.ReadWriteSpan
import io.opentelemetry.kotlin.tracing.model.ReadableSpan
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
 * A simple span processor that immediately exports spans to a [SpanExporter].
 *
 * https://opentelemetry.io/docs/specs/otel/trace/sdk/#built-in-span-processors
 */
internal class SimpleSpanProcessor(
    private val exporter: SpanExporter,
    private val scope: CoroutineScope,
    private val sdkErrorHandler: SdkErrorHandler,
) : SpanProcessor {

    private val exportMutex = Mutex()
    private val shutdownState = MutableShutdownState()

    // Tracks launched export jobs so forceFlush/shutdown can wait for them. The lock is
    // needed because onEnd() is non-suspend and must register synchronously.
    private val pendingExports = mutableSetOf<Job>()
    private val pendingExportsLock = ReentrantReadWriteLock()

    /** Test hook fired inside [pendingExportsLock] after registration, before the job starts. */
    internal var beforeJobStart: (() -> Unit)? = null

    override fun onStart(
        span: ReadWriteSpan,
        parentContext: Context
    ) {
    }

    override fun onEnding(span: ReadWriteSpan) {
    }

    override fun onEnd(span: ReadableSpan) {
        if (!span.spanContext.traceFlags.isSampled) {
            return
        }
        val job = admitExport(span) ?: return
        job.start()
    }

    /** Atomically admits [span] under [pendingExportsLock]; the job starts after release. */
    private fun admitExport(span: ReadableSpan): Job? = pendingExportsLock.write {
        if (shutdownState.isShutdown) {
            null
        } else {
            scope.launch(start = CoroutineStart.LAZY) {
                exportMutex.withLock { exportSpan(span) }
            }.also { newJob ->
                pendingExports.add(newJob)
                newJob.invokeOnCompletion {
                    pendingExportsLock.write { pendingExports.remove(newJob) }
                }
                beforeJobStart?.invoke()
            }
        }
    }

    private suspend fun exportSpan(span: ReadableSpan) {
        sdkErrorHandler.guardExporterCode("Export failed") {
            try {
                val result = withTimeout(BatchTelemetryDefaults.EXPORT_TIMEOUT_MS) {
                    exporter.export(listOf(span))
                }
                if (result == OperationResultCode.Failure) {
                    sdkErrorHandler.reportError(
                        SdkError.UserCodeError(
                            ExportUnsuccessfulException(),
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

    override fun isStartRequired(): Boolean = false
    override fun isEndRequired(): Boolean = true
    override fun isOnEndingRequired(): Boolean = false

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
            // export admitted before the snapshot is tracked and any later onEnd is rejected.
            val drainResult = shutdownState.shutdown(BatchTelemetryDefaults.SHUTDOWN_TIMEOUT_MS) {
                ownsShutdown = true
                snapshotPendingExports().forEach { it.join() }
                OperationResultCode.Success
            }
            completed = drainResult
            // Phase 2 runs even when draining timed out or failed, so the exporter is always
            // released. Losers of the shutdown race return Success without cleaning up again.
            return if (ownsShutdown) {
                drainResult.combine(cleanupExporter())
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

private class ExportUnsuccessfulException :
    Exception("SpanExporter.export returned OperationResultCode.Failure")
