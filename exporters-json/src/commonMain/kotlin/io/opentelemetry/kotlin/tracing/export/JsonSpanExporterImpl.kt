package io.opentelemetry.kotlin.tracing.export

import io.opentelemetry.kotlin.encode.JsonSpanEncoder
import io.opentelemetry.kotlin.encode.OtlpJsonEncoder
import io.opentelemetry.kotlin.export.MutableShutdownState
import io.opentelemetry.kotlin.export.OperationResultCode
import io.opentelemetry.kotlin.tracing.data.SpanData

internal class JsonSpanExporterImpl(
    val encoder: OtlpJsonEncoder<SpanData> = JsonSpanEncoder()
) : JsonSpanExporter {
    private val shutdownState = MutableShutdownState()

    override suspend fun export(telemetry: List<SpanData>): OperationResultCode =
        shutdownState.ifActive {
            val result = telemetry.map {
                encoder.encode(it)
            }
            OperationResultCode.Success
        }

    override suspend fun forceFlush(): OperationResultCode = OperationResultCode.Success

    override suspend fun shutdown(): OperationResultCode =
        shutdownState.shutdown {
            OperationResultCode.Success
        }
}
