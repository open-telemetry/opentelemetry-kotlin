package io.opentelemetry.kotlin.tracing.export

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.export.OperationResultCode
import io.opentelemetry.kotlin.tracing.data.SpanData

@OptIn(ExperimentalApi::class)
class FakeSpanExporter(
    val exportReturnValue: (List<SpanData>) -> OperationResultCode = { OperationResultCode.Success },
    val forceFlushReturnValue: () -> OperationResultCode = { OperationResultCode.Success },
    val shutdownReturnValue: () -> OperationResultCode = { OperationResultCode.Success },
) : SpanExporter {

    val exports = mutableListOf<SpanData>()

    override fun export(telemetry: List<SpanData>): OperationResultCode {
        exports += telemetry
        return exportReturnValue(telemetry)
    }

    override fun forceFlush(): OperationResultCode = forceFlushReturnValue()
    override fun shutdown(): OperationResultCode = shutdownReturnValue()
}
