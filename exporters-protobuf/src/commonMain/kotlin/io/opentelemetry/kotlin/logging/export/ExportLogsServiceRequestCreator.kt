package io.opentelemetry.kotlin.logging.export

import io.opentelemetry.kotlin.export.conversion.toResource
import io.opentelemetry.kotlin.export.conversion.toInstrumentationScopeInfo
import io.opentelemetry.kotlin.export.conversion.toProtobuf
import io.opentelemetry.kotlin.logging.data.LogRecordData
import io.opentelemetry.proto.collector.logs.v1.ExportLogsServiceRequest
import io.opentelemetry.proto.logs.v1.ResourceLogs
import io.opentelemetry.proto.logs.v1.ScopeLogs

fun List<LogRecordData>.toProtobufByteArray(): ByteArray =
    ExportLogsServiceRequest.ADAPTER.encode(toExportLogsServiceRequest())

fun ByteArray.toLogRecordDataList(): List<LogRecordData> {
    val request = ExportLogsServiceRequest.ADAPTER.decode(this)
    return request.resource_logs.flatMap { resourceLogs ->
        val resource = resourceLogs.resource?.toResource()
            ?: return@flatMap emptyList()
        resourceLogs.scope_logs.flatMap { scopeLogs ->
            val scopeInfo = scopeLogs.scope?.toInstrumentationScopeInfo(scopeLogs.schema_url)
                ?: return@flatMap emptyList()
            scopeLogs.log_records.map { logRecord ->
                logRecord.toLogRecordData(resource, scopeInfo)
            }
        }
    }
}

internal fun List<LogRecordData>.toExportLogsServiceRequest(): ExportLogsServiceRequest =
    ExportLogsServiceRequest(
        resource_logs = toResourceLogs()
    )

private fun List<LogRecordData>.toResourceLogs(): List<ResourceLogs> = map { it.toResourceLogs() }

private fun LogRecordData.toResourceLogs(): ResourceLogs = ResourceLogs(
    scope_logs = listOf(toScopeLogs()),
    resource = resource.toProtobuf()
)

private fun LogRecordData.toScopeLogs(): ScopeLogs = ScopeLogs(
    log_records = listOf(toProtobuf()),
    scope = instrumentationScopeInfo.toProtobuf(),
    schema_url = instrumentationScopeInfo.schemaUrl ?: ""
)
