package io.opentelemetry.kotlin.tracing.export

import io.opentelemetry.kotlin.export.OperationResultCode
import io.opentelemetry.kotlin.tracing.data.SpanData

class FakeSpanExporter(
    val exportReturnValue: (List<SpanData>) -> OperationResultCode = { OperationResultCode.Success },
    val forceFlushReturnValue: () -> OperationResultCode = { OperationResultCode.Success },
    val shutdownReturnValue: () -> OperationResultCode = { OperationResultCode.Success },
) : SpanExporter {

    val exports = mutableListOf<SpanData>()

    override suspend fun export(telemetry: List<SpanData>): OperationResultCode {
        exports += telemetry
        return exportReturnValue(telemetry)
    }

    override suspend fun forceFlush(): OperationResultCode = forceFlushReturnValue()
    override suspend fun shutdown(): OperationResultCode = shutdownReturnValue()
}
