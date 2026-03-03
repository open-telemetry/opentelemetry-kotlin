package io.opentelemetry.kotlin.logging.export

import io.opentelemetry.proto.collector.logs.v1.ExportLogsServiceResponse

fun ByteArray.deserializeLogRecordErrorMessage() : String? =
    ExportLogsServiceResponse.ADAPTER.decode(this).partial_success?.error_message