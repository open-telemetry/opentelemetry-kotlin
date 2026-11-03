package io.opentelemetry.kotlin.tracing.export

import io.opentelemetry.kotlin.ReentrantReadWriteLock
import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.error.SdkErrorHandler
import io.opentelemetry.kotlin.export.BatchTelemetryDefaults
import io.opentelemetry.kotlin.export.MutableShutdownState
import io.opentelemetry.kotlin.export.OperationResultCode
import io.opentelemetry.kotlin.export.and
import io.opentelemetry.kotlin.export.guardExporterCode
import io.opentelemetry.kotlin.export.runWithTimeout
import io.opentelemetry.kotlin.tracing.model.ReadWriteSpan
import io.opentelemetry.kotlin.tracing.model.ReadableSpan
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

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

    // Tracks every launched export job so that forceFlush/shutdown can wait for them.
    // A ReentrantReadWriteLock is used because onEnd() is non-suspend and must register
    // jobs synchronously, while forceFlush/shutdown (suspend) need a consistent snapshot.
    private val pendingExports = mutableSetOf<Job>()
    private val pendingExportsLock = ReentrantReadWriteLock()

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
        shutdownState.execute {
            val job = scope.launch {
                exportMutex.withLock {
                    sdkErrorHandler.guardExporterCode("Export failed") {
                        runWithTimeout(BatchTelemetryDefaults.EXPORT_TIMEOUT_MS) {
                            exporter.export(listOf(span))
                        }
                    }
                }
            }
            registerJob(job)
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
     * from the non-suspend [onEnd] path.
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
