package io.opentelemetry.kotlin.tracing.export

import io.opentelemetry.kotlin.export.conversion.toResource
import io.opentelemetry.kotlin.export.conversion.toInstrumentationScopeInfo
import io.opentelemetry.kotlin.export.conversion.toProtobuf
import io.opentelemetry.kotlin.tracing.data.SpanData
import io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceRequest
import io.opentelemetry.proto.trace.v1.ResourceSpans
import io.opentelemetry.proto.trace.v1.ScopeSpans

fun List<SpanData>.toProtobufByteArray() =
    ExportTraceServiceRequest.ADAPTER.encode(toExportTraceServiceRequest())

fun ByteArray.toSpanDataList(): List<SpanData> {
    val request = ExportTraceServiceRequest.ADAPTER.decode(this)
    return request.resource_spans.flatMap { resourceSpans ->
        val resource = resourceSpans.resource?.toResource()
            ?: return@flatMap emptyList()
        resourceSpans.scope_spans.flatMap { scopeSpans ->
            val scopeInfo = scopeSpans.scope?.toInstrumentationScopeInfo(scopeSpans.schema_url)
                ?: return@flatMap emptyList()
            scopeSpans.spans.map { span ->
                span.toSpanData(resource, scopeInfo)
            }
        }
    }
}

internal fun List<SpanData>.toExportTraceServiceRequest(): ExportTraceServiceRequest =
    ExportTraceServiceRequest(toResourceSpan())

private fun List<SpanData>.toResourceSpan(): List<ResourceSpans> = map { it.toResourceSpan() }

private fun SpanData.toResourceSpan(): ResourceSpans = ResourceSpans(
    scope_spans = listOf(toScopedSpan()),
    resource = resource.toProtobuf()
)

private fun SpanData.toScopedSpan(): ScopeSpans = ScopeSpans(
    spans = listOf(toProtobuf()),
    scope = instrumentationScopeInfo.toProtobuf(),
    schema_url = instrumentationScopeInfo.schemaUrl ?: ""
)
