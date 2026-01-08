package io.opentelemetry.kotlin.tracing.export

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.export.conversion.toProtobuf
import io.opentelemetry.kotlin.tracing.data.SpanData
import io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceRequest
import io.opentelemetry.proto.trace.v1.ResourceSpans
import io.opentelemetry.proto.trace.v1.ScopeSpans

@OptIn(ExperimentalApi::class)
fun List<SpanData>.toProtobufByteArray() =
    ExportTraceServiceRequest.ADAPTER.encode(toExportTraceServiceRequest())

@OptIn(ExperimentalApi::class)
internal fun List<SpanData>.toExportTraceServiceRequest(): ExportTraceServiceRequest =
    ExportTraceServiceRequest(toResourceSpan())

@OptIn(ExperimentalApi::class)
private fun List<SpanData>.toResourceSpan(): List<ResourceSpans> = map { it.toResourceSpan() }

@OptIn(ExperimentalApi::class)
private fun SpanData.toResourceSpan(): ResourceSpans = ResourceSpans(
    scope_spans = listOf(toScopedSpan()),
    resource = resource.toProtobuf()
)

@OptIn(ExperimentalApi::class)
private fun SpanData.toScopedSpan(): ScopeSpans = ScopeSpans(
    spans = listOf(toProtobuf()),
    scope = instrumentationScopeInfo.toProtobuf(),
    schema_url = instrumentationScopeInfo.schemaUrl ?: ""
)

