package io.opentelemetry.kotlin.tracing.export

import io.opentelemetry.kotlin.ReentrantReadWriteLock
import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.export.BatchTelemetryDefaults
import io.opentelemetry.kotlin.export.MutableShutdownState
import io.opentelemetry.kotlin.export.OperationResultCode
import io.opentelemetry.kotlin.export.runWithTimeout
import io.opentelemetry.kotlin.tracing.model.ReadWriteSpan
import io.opentelemetry.kotlin.tracing.model.ReadableSpan
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * A simple span processor that immediately exports spans to a [SpanExporter].
 *
 * https://opentelemetry.io/docs/specs/otel/trace/sdk/#built-in-span-processors
 */
internal class SimpleSpanProcessor(
    private val exporter: SpanExporter,
    private val scope: CoroutineScope,
) : SpanProcessor {

    private val lock = ReentrantReadWriteLock()
    private val shutdownState = MutableShutdownState()

    override fun onStart(
        span: ReadWriteSpan,
        parentContext: Context
    ) {
    }

    override fun onEnding(span: ReadWriteSpan) {
    }

    override fun onEnd(span: ReadableSpan) {
        shutdownState.execute {
            scope.launch {
                lock.write {
                    exporter.export(listOf(span))
                }
            }
        }
    }

    override fun isStartRequired(): Boolean = true
    override fun isEndRequired(): Boolean = true

    override suspend fun forceFlush(): OperationResultCode =
        runWithTimeout(BatchTelemetryDefaults.FORCE_FLUSH_TIMEOUT_MS) { exporter.forceFlush() }

    override suspend fun shutdown(): OperationResultCode =
        shutdownState.shutdown(BatchTelemetryDefaults.SHUTDOWN_TIMEOUT_MS, exporter::shutdown)
}
