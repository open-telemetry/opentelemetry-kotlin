package io.opentelemetry.kotlin.tracing.export

import io.opentelemetry.proto.collector.logs.v1.ExportLogsServiceResponse

fun ByteArray.deserializeTraceRecordErrorMessage() =
    ExportLogsServiceResponse.ADAPTER.decode(this).partial_success?.error_message