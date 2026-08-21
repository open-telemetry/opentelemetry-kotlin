package io.opentelemetry.kotlin.tracing.export

import io.opentelemetry.kotlin.aliases.OtelJavaSpanExporter
import io.opentelemetry.kotlin.awaitOperationResultCode
import io.opentelemetry.kotlin.export.MutableShutdownState
import io.opentelemetry.kotlin.export.OperationResultCode
import io.opentelemetry.kotlin.tracing.data.SpanData
import io.opentelemetry.kotlin.tracing.ext.toOtelJavaSpanData

internal class SpanExporterAdapter(
    private val impl: OtelJavaSpanExporter
) : SpanExporter {

    private val shutdownState = MutableShutdownState()

    override suspend fun export(telemetry: List<SpanData>): OperationResultCode =
        shutdownState.ifActive {
            awaitOperationResultCode {
                impl.export(telemetry.map(SpanData::toOtelJavaSpanData))
            }
        }

    override suspend fun forceFlush(): OperationResultCode =
        awaitOperationResultCode { impl.flush() }

    override suspend fun shutdown(): OperationResultCode =
        shutdownState.shutdown {
            awaitOperationResultCode { impl.shutdown() }
        }
}
