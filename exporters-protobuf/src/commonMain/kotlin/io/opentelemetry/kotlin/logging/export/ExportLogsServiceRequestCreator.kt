package io.opentelemetry.kotlin.logging.export

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.export.conversion.toResource
import io.opentelemetry.kotlin.export.conversion.toInstrumentationScopeInfo
import io.opentelemetry.kotlin.export.conversion.toProtobuf
import io.opentelemetry.kotlin.logging.model.ReadableLogRecord
import io.opentelemetry.proto.collector.logs.v1.ExportLogsServiceRequest
import io.opentelemetry.proto.logs.v1.ResourceLogs
import io.opentelemetry.proto.logs.v1.ScopeLogs

@OptIn(ExperimentalApi::class)
fun List<ReadableLogRecord>.toProtobufByteArray(): ByteArray =
    ExportLogsServiceRequest.ADAPTER.encode(toExportLogsServiceRequest())

@OptIn(ExperimentalApi::class)
fun ByteArray.toReadableLogRecordList(): List<ReadableLogRecord> {
    val request = ExportLogsServiceRequest.ADAPTER.decode(this)
    return request.resource_logs.flatMap { resourceLogs ->
        val resource = resourceLogs.resource?.toResource()
            ?: return@flatMap emptyList()
        resourceLogs.scope_logs.flatMap { scopeLogs ->
            val scopeInfo = scopeLogs.scope?.toInstrumentationScopeInfo(scopeLogs.schema_url)
                ?: return@flatMap emptyList()
            scopeLogs.log_records.map { logRecord ->
                logRecord.toReadableLogRecord(resource, scopeInfo)
            }
        }
    }
}

@OptIn(ExperimentalApi::class)
internal fun List<ReadableLogRecord>.toExportLogsServiceRequest(): ExportLogsServiceRequest =
    ExportLogsServiceRequest(
        resource_logs = toResourceLogs()
    )

@OptIn(ExperimentalApi::class)
private fun List<ReadableLogRecord>.toResourceLogs(): List<ResourceLogs> = map { it.toResourceLogs() }

@OptIn(ExperimentalApi::class)
private fun ReadableLogRecord.toResourceLogs(): ResourceLogs = ResourceLogs(
    scope_logs = listOf(toScopeLogs()),
    resource = resource.toProtobuf()
)

@OptIn(ExperimentalApi::class)
private fun ReadableLogRecord.toScopeLogs(): ScopeLogs = ScopeLogs(
    log_records = listOf(toProtobuf()),
    scope = instrumentationScopeInfo.toProtobuf(),
    schema_url = instrumentationScopeInfo.schemaUrl ?: ""
)
