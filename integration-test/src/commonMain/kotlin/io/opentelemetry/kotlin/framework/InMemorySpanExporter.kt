package io.opentelemetry.kotlin.framework

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.export.OperationResultCode
import io.opentelemetry.kotlin.tracing.data.SpanData
import io.opentelemetry.kotlin.tracing.export.SpanExporter
import kotlin.collections.plusAssign

@OptIn(ExperimentalApi::class)
internal class InMemorySpanExporter : SpanExporter {

    private val impl = mutableListOf<SpanData>()

    val exportedSpans: List<SpanData>
        get() = impl

    override fun export(telemetry: List<SpanData>): OperationResultCode {
        impl += telemetry
        return OperationResultCode.Success
    }

    override suspend fun forceFlush(): OperationResultCode = OperationResultCode.Success
    override suspend fun shutdown(): OperationResultCode = OperationResultCode.Success
}
