@file:Suppress("DEPRECATION", "TYPEALIAS_EXPANSION_DEPRECATION")

package io.opentelemetry.kotlin.tracing.data

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaSpanData
import io.opentelemetry.kotlin.aliases.OtelJavaStatusData
import io.opentelemetry.kotlin.attributes.attrsFromMap
import io.opentelemetry.kotlin.attributes.resourceFromMap
import io.opentelemetry.kotlin.scope.toOtelJavaInstrumentationScopeInfo
import io.opentelemetry.kotlin.tracing.ext.toOtelJavaSpanKind
import io.opentelemetry.kotlin.tracing.ext.toOtelJavaStatusCode
import io.opentelemetry.kotlin.tracing.model.ReadWriteSpanAdapter
import io.opentelemetry.kotlin.tracing.model.ReadableSpanAdapter
import io.opentelemetry.kotlin.tracing.toOtelJavaSpanContext

/**
 * Converts opentelemetry-kotlin [SpanData] to opentelemetry-java span data.
 * Existing Java span data adapters are unwrapped without copying.
 */
@ExperimentalApi
public fun SpanData.toOtelJavaSpanData(): OtelJavaSpanData = when (this) {
    is SpanDataAdapter -> impl
    is ReadableSpanAdapter -> impl.toSpanData()
    is ReadWriteSpanAdapter -> impl.toSpanData()
    else -> convertToOtelJavaSpanData()
}

private fun SpanData.convertToOtelJavaSpanData(): OtelJavaSpanData {
    return OtelJavaSpanDataImpl(
        nameImpl = name,
        statusImpl = OtelJavaStatusData.create(status.statusCode.toOtelJavaStatusCode(), status.description),
        parentSpanContextImpl = parent.toOtelJavaSpanContext(),
        spanContextImpl = spanContext.toOtelJavaSpanContext(),
        kindImpl = spanKind.toOtelJavaSpanKind(),
        startEpochNanosImpl = startTimestamp,
        endEpochNanosImpl = endTimestamp ?: 0,
        resourceImpl = resource.let(::resourceFromMap),
        scopeImpl = instrumentationScopeInfo.toOtelJavaInstrumentationScopeInfo(),
        attributesImpl = attrsFromMap(attributes),
        totalAttributeCountImpl = attributes.size + droppedAttributesCount,
        eventsImpl = events.map { it.toOtelJavaEventData() },
        totalRecordedEventsImpl = events.size + droppedEventsCount,
        linksImpl = links.map { it.toOtelJavaLinkData() },
        totalRecordedLinksImpl = links.size + droppedLinksCount,
        hasEndedImpl = hasEnded
    )
}
