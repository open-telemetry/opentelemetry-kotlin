package io.opentelemetry.kotlin.tracing.export

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaSpanExporter
import io.opentelemetry.kotlin.export.OperationResultCode
import io.opentelemetry.kotlin.toOperationResultCode
import io.opentelemetry.kotlin.tracing.data.SpanData
import io.opentelemetry.kotlin.tracing.ext.toOtelJavaSpanData

@OptIn(ExperimentalApi::class)
internal class SpanExporterAdapter(
    private val impl: OtelJavaSpanExporter
) : SpanExporter {

    override fun export(telemetry: List<SpanData>): OperationResultCode {
        val code = impl.export(telemetry.map(SpanData::toOtelJavaSpanData))
        return code.toOperationResultCode()
    }

    override fun shutdown(): OperationResultCode = impl.shutdown().toOperationResultCode()
    override fun forceFlush(): OperationResultCode = impl.flush().toOperationResultCode()
}
