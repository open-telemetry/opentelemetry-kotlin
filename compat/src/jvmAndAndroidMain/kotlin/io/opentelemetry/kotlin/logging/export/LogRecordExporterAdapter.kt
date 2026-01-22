package io.opentelemetry.kotlin.logging.export

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaLogRecordExporter
import io.opentelemetry.kotlin.export.OperationResultCode
import io.opentelemetry.kotlin.logging.model.ReadableLogRecord
import io.opentelemetry.kotlin.toOperationResultCode

@OptIn(ExperimentalApi::class)
internal class LogRecordExporterAdapter(
    private val impl: OtelJavaLogRecordExporter
) : LogRecordExporter {

    override suspend fun export(telemetry: List<ReadableLogRecord>): OperationResultCode {
        val code = impl.export(telemetry.map(ReadableLogRecord::toLogRecordData))
        return code.toOperationResultCode()
    }

    override suspend fun shutdown(): OperationResultCode = impl.shutdown().toOperationResultCode()
    override suspend fun forceFlush(): OperationResultCode = impl.flush().toOperationResultCode()
}
