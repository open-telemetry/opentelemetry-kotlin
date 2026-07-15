package io.opentelemetry.kotlin.logging.export

import io.opentelemetry.kotlin.export.OtlpPartialSuccess
import io.opentelemetry.proto.collector.logs.v1.ExportLogsServiceResponse

/**
 * Parses the `partial_success` field of a log export response, returning null when it is absent,
 * empty (equivalent to absent per the OTLP spec), or the body cannot be decoded.
 */
fun ByteArray.deserializeLogRecordPartialSuccess(): OtlpPartialSuccess? =
    runCatching { ExportLogsServiceResponse.ADAPTER.decode(this).partial_success }
        .getOrNull()
        ?.takeIf { it.rejected_log_records > 0L || it.error_message.isNotEmpty() }
        ?.let { OtlpPartialSuccess(it.rejected_log_records, it.error_message) }

fun ByteArray.deserializeLogRecordErrorMessage(): String? =
    deserializeLogRecordPartialSuccess()?.errorMessage
