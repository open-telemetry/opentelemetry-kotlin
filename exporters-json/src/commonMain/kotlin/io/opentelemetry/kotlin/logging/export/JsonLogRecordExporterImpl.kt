package io.opentelemetry.kotlin.logging.export

import io.opentelemetry.kotlin.encode.JsonLogRecordEncoder
import io.opentelemetry.kotlin.encode.OtlpJsonEncoder
import io.opentelemetry.kotlin.export.MutableShutdownState
import io.opentelemetry.kotlin.export.OperationResultCode
import io.opentelemetry.kotlin.logging.model.ReadableLogRecord

internal class JsonLogRecordExporterImpl(
    val encoder: OtlpJsonEncoder<ReadableLogRecord> = JsonLogRecordEncoder()
) : JsonLogRecordExporter {
    private val shutdownState = MutableShutdownState()

    override suspend fun export(telemetry: List<ReadableLogRecord>): OperationResultCode =
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
