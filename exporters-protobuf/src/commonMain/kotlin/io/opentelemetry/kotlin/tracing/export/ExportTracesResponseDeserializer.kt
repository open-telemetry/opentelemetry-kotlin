package io.opentelemetry.kotlin.tracing.export

import io.opentelemetry.kotlin.export.OtlpPartialSuccess
import io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceResponse

/**
 * Parses the `partial_success` field of a trace export response, returning null when it is absent,
 * empty (equivalent to absent per the OTLP spec), or the body cannot be decoded.
 */
fun ByteArray.deserializeTraceRecordPartialSuccess(): OtlpPartialSuccess? =
    runCatching { ExportTraceServiceResponse.ADAPTER.decode(this).partial_success }
        .getOrNull()
        ?.takeIf { it.rejected_spans > 0L || it.error_message.isNotEmpty() }
        ?.let { OtlpPartialSuccess(it.rejected_spans, it.error_message) }

fun ByteArray.deserializeTraceRecordErrorMessage(): String? =
    deserializeTraceRecordPartialSuccess()?.errorMessage
