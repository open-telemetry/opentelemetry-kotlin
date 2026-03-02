package io.opentelemetry.kotlin.tracing.export

import io.opentelemetry.kotlin.export.OperationResultCode
import io.opentelemetry.kotlin.tracing.data.SpanData

internal class InMemorySpanExporterImpl : InMemorySpanExporter {

    private val impl = mutableListOf<SpanData>()

    override val exportedSpans: List<SpanData>
        get() = impl

    override suspend fun export(telemetry: List<SpanData>): OperationResultCode {
        impl += telemetry
        return OperationResultCode.Success
    }

    override suspend fun forceFlush(): OperationResultCode = OperationResultCode.Success
    override suspend fun shutdown(): OperationResultCode = OperationResultCode.Success
}
