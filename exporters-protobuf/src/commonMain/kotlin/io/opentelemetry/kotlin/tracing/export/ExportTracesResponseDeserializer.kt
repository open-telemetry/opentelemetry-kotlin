package io.opentelemetry.kotlin.tracing.export

import io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceResponse

fun ByteArray.deserializeTraceRecordErrorMessage() =
    ExportTraceServiceResponse.ADAPTER.decode(this).partial_success?.error_message