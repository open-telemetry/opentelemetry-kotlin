package io.opentelemetry.kotlin.tracing.export

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.ReentrantReadWriteLock
import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.error.SdkErrorHandler
import io.opentelemetry.kotlin.export.CompositeTelemetryCloseable
import io.opentelemetry.kotlin.export.OperationResultCode
import io.opentelemetry.kotlin.export.TelemetryCloseable
import io.opentelemetry.kotlin.export.batchExportOperation
import io.opentelemetry.kotlin.tracing.model.ReadWriteSpan
import io.opentelemetry.kotlin.tracing.model.ReadableSpan

@OptIn(ExperimentalApi::class)
internal class CompositeSpanProcessor(
    private val processors: List<SpanProcessor>,
    private val sdkErrorHandler: SdkErrorHandler,
    private val telemetryCloseable: TelemetryCloseable = CompositeTelemetryCloseable(
        processors,
        sdkErrorHandler
    ),
) : SpanProcessor, TelemetryCloseable by telemetryCloseable {

    private val lock = ReentrantReadWriteLock()

    override fun onStart(
        span: ReadWriteSpan,
        parentContext: Context
    ) {
        lock.write {
            batchExportOperation(
                processors,
                sdkErrorHandler
            ) {
                if (it.isStartRequired()) {
                    it.onStart(span, parentContext)
                }
                OperationResultCode.Success
            }
        }
    }

    override fun onEnding(span: ReadWriteSpan) {
        lock.write {
            batchExportOperation(
                processors,
                sdkErrorHandler
            ) {
                if (it.isEndRequired()) {
                    it.onEnding(span)
                }
                OperationResultCode.Success
            }
        }
    }

    override fun onEnd(span: ReadableSpan) {
        lock.write {
            batchExportOperation(
                processors,
                sdkErrorHandler
            ) {
                if (it.isEndRequired()) {
                    it.onEnd(span)
                }
                OperationResultCode.Success
            }
        }
    }

    override fun isStartRequired(): Boolean = true
    override fun isEndRequired(): Boolean = true
}
