package io.opentelemetry.kotlin.logging.export

import io.opentelemetry.kotlin.aliases.OtelJavaLogRecordExporter
import io.opentelemetry.kotlin.awaitOperationResultCode
import io.opentelemetry.kotlin.export.MutableShutdownState
import io.opentelemetry.kotlin.export.OperationResultCode
import io.opentelemetry.kotlin.logging.data.LogRecordData

internal class LogRecordExporterAdapter(
    private val impl: OtelJavaLogRecordExporter
) : LogRecordExporter {

    private val shutdownState = MutableShutdownState()

    override suspend fun export(telemetry: List<LogRecordData>): OperationResultCode =
        shutdownState.ifActive {
            awaitOperationResultCode {
                impl.export(telemetry.map(LogRecordData::toOtelJavaLogRecordData))
            }
        }

    override suspend fun forceFlush(): OperationResultCode =
        awaitOperationResultCode { impl.flush() }

    override suspend fun shutdown(): OperationResultCode =
        shutdownState.shutdown {
            awaitOperationResultCode { impl.shutdown() }
        }
}
