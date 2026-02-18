package io.opentelemetry.kotlin.tracing.export

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.ReentrantReadWriteLock
import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.export.MutableShutdownState
import io.opentelemetry.kotlin.export.OperationResultCode
import io.opentelemetry.kotlin.tracing.model.ReadWriteSpan
import io.opentelemetry.kotlin.tracing.model.ReadableSpan
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * A simple span processor that immediately exports spans to a [SpanExporter].
 *
 * https://opentelemetry.io/docs/specs/otel/trace/sdk/#built-in-span-processors
 */
@OptIn(ExperimentalApi::class)
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
    override suspend fun forceFlush(): OperationResultCode = exporter.forceFlush()

    override suspend fun shutdown(): OperationResultCode =
        shutdownState.ifActive(OperationResultCode.Success) {
            shutdownState.shutdown()
            exporter.shutdown()
        }
}
