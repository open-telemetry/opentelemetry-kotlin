package io.opentelemetry.kotlin.tracing.export

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.error.SdkErrorHandler
import io.opentelemetry.kotlin.export.CompositeTelemetryCloseable
import io.opentelemetry.kotlin.export.OperationResultCode
import io.opentelemetry.kotlin.export.TelemetryCloseable
import io.opentelemetry.kotlin.export.batchExportOperationSuspend
import io.opentelemetry.kotlin.tracing.data.SpanData

@OptIn(ExperimentalApi::class)
internal class CompositeSpanExporter(
    private val exporters: List<SpanExporter>,
    private val sdkErrorHandler: SdkErrorHandler,
    private val telemetryCloseable: CompositeTelemetryCloseable = CompositeTelemetryCloseable(
        exporters,
        sdkErrorHandler,
    )
) : SpanExporter, TelemetryCloseable by telemetryCloseable {

    override suspend fun export(telemetry: List<SpanData>): OperationResultCode =
        batchExportOperationSuspend(
            exporters,
            sdkErrorHandler
        ) {
            it.export(telemetry)
        }
}
