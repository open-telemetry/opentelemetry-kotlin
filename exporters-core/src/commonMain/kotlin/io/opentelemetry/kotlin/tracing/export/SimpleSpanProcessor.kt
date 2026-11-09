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
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.async
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

    // Tracks launched export jobs so forceFlush/shutdown can wait for them and collect
    // their results. The lock is needed because onEnd() is non-suspend and must register
    // synchronously.
    private val pendingExports = mutableSetOf<Deferred<OperationResultCode>>()
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
    private fun admitExport(span: ReadableSpan): Deferred<OperationResultCode>? =
        pendingExportsLock.write {
            if (shutdownState.isShutdown) {
                null
            } else {
                scope.async(start = CoroutineStart.LAZY) {
                    exportMutex.withLock { exportSpan(span) }
                }.also { deferred ->
                    pendingExports.add(deferred)
                    deferred.invokeOnCompletion {
                        pendingExportsLock.write { pendingExports.remove(deferred) }
                    }
                    beforeJobStart?.invoke()
                }
            }
        }

    private suspend fun exportSpan(span: ReadableSpan): OperationResultCode =
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

    override fun isStartRequired(): Boolean = false
    override fun isEndRequired(): Boolean = true
    override fun isOnEndingRequired(): Boolean = false

    override suspend fun forceFlush(): OperationResultCode {
        if (shutdownState.isShutdown) {
            return OperationResultCode.Success
        }
        return runWithTimeout(BatchTelemetryDefaults.FORCE_FLUSH_TIMEOUT_MS) {
            val exportResult = drainPendingExports()
            // Recheck shutdown inside the mutex: a concurrent shutdown may have closed
            // the exporter while this flush waited.
            exportResult.combine(
                boundedExporterCall("Exporter forceFlush failed", skipIfShutdown = true) {
                    exporter.forceFlush()
                }
            )
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
                drainPendingExports()
            }
            completed = drainResult
            // Cleanup runs under NonCancellable so caller cancellation cannot skip the
            // exporter's forceFlush/shutdown once this processor owns shutdown.
            return if (ownsShutdown) {
                drainResult.combine(withContext(NonCancellable) { cleanupExporter() })
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
        skipIfShutdown: Boolean = false,
        call: suspend () -> OperationResultCode,
    ): OperationResultCode = runWithTimeout(BatchTelemetryDefaults.FORCE_FLUSH_TIMEOUT_MS) {
        exportMutex.withLock {
            // skipIfShutdown: a delayed flush must not touch an exporter shutdown closed;
            // shutdown's own cleanup passes false.
            if (skipIfShutdown && shutdownState.isShutdown) {
                OperationResultCode.Success
            } else {
                sdkErrorHandler.guardExporterCode(message, call)
            }
        }
    }

    /** Waits for a fresh snapshot of pending exports and folds their results into one code. */
    private suspend fun drainPendingExports(): OperationResultCode {
        val exports = snapshotPendingExports()
        exports.forEach { it.join() }
        var result: OperationResultCode = OperationResultCode.Success
        exports.forEach { deferred ->
            if (!deferred.isCancelled && deferred.await() == OperationResultCode.Failure) {
                result = OperationResultCode.Failure
            }
        }
        return result
    }

    private fun OperationResultCode.combine(other: OperationResultCode): OperationResultCode =
        if (this == OperationResultCode.Success && other == OperationResultCode.Success) {
            OperationResultCode.Success
        } else {
            OperationResultCode.Failure
        }

    private fun snapshotPendingExports(): List<Deferred<OperationResultCode>> =
        pendingExportsLock.read { pendingExports.toList() }
}

private class ExportUnsuccessfulException :
    Exception("SpanExporter.export returned OperationResultCode.Failure")
