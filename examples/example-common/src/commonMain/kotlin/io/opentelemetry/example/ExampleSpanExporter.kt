package io.opentelemetry.example

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.export.OperationResultCode
import io.opentelemetry.kotlin.tracing.data.SpanData
import io.opentelemetry.kotlin.tracing.export.SpanExporter

@OptIn(ExperimentalApi::class)
internal class ExampleSpanExporter : SpanExporter {

    override fun export(telemetry: List<SpanData>): OperationResultCode {
        telemetry.forEach { span ->
            println("Exporting span: $span")
        }
        return OperationResultCode.Success
    }

    override fun forceFlush(): OperationResultCode = OperationResultCode.Success
    override fun shutdown(): OperationResultCode = OperationResultCode.Success
}
