package io.opentelemetry.kotlin.tracing.export

import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.error.SdkErrorHandler
import io.opentelemetry.kotlin.export.CompositeTelemetryCloseable
import io.opentelemetry.kotlin.export.OperationResultCode
import io.opentelemetry.kotlin.export.TelemetryCloseable
import io.opentelemetry.kotlin.export.batchExportOperation
import io.opentelemetry.kotlin.tracing.model.ReadWriteSpan
import io.opentelemetry.kotlin.tracing.model.ReadableSpan

internal class CompositeSpanProcessor(
    private val processors: List<SpanProcessor>,
    private val sdkErrorHandler: SdkErrorHandler,
    private val telemetryCloseable: TelemetryCloseable = CompositeTelemetryCloseable(
        processors,
        sdkErrorHandler
    ),
) : SpanProcessor, TelemetryCloseable by telemetryCloseable {

    override fun onStart(
        span: ReadWriteSpan,
        parentContext: Context
    ) {
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

    override fun onEnding(span: ReadWriteSpan) {
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

    override fun onEnd(span: ReadableSpan) {
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

    override fun isStartRequired(): Boolean = true
    override fun isEndRequired(): Boolean = true
}
