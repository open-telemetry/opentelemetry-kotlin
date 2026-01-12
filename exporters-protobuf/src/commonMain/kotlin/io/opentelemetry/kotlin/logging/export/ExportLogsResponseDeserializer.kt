package io.opentelemetry.kotlin.logging.export

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.proto.collector.logs.v1.ExportLogsServiceResponse

@OptIn(ExperimentalApi::class)
fun ByteArray.deserializeLogRecordErrorMessage() : String? =
    ExportLogsServiceResponse.ADAPTER.decode(this).partial_success?.error_message