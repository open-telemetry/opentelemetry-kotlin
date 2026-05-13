package io.opentelemetry.kotlin.tracing.export

import io.opentelemetry.kotlin.export.OperationResultCode
import io.opentelemetry.kotlin.tracing.data.SpanData

internal object NoopSpanExporter : SpanExporter {
    override suspend fun export(telemetry: List<SpanData>): OperationResultCode = OperationResultCode.Failure
    override suspend fun forceFlush(): OperationResultCode = OperationResultCode.Success
    override suspend fun shutdown(): OperationResultCode = OperationResultCode.Success
}
