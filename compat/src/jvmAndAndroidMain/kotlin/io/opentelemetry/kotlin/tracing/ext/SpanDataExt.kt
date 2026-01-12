@file:Suppress("DEPRECATION", "TYPEALIAS_EXPANSION_DEPRECATION")

package io.opentelemetry.kotlin.tracing.ext

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaInstrumentationLibraryInfo
import io.opentelemetry.kotlin.aliases.OtelJavaSpanData
import io.opentelemetry.kotlin.aliases.OtelJavaStatusData
import io.opentelemetry.kotlin.attributes.attrsFromMap
import io.opentelemetry.kotlin.attributes.resourceFromMap
import io.opentelemetry.kotlin.tracing.data.OtelJavaSpanDataImpl
import io.opentelemetry.kotlin.tracing.data.SpanData

@OptIn(ExperimentalApi::class)
internal fun SpanData.toOtelJavaSpanData(): OtelJavaSpanData {
    return OtelJavaSpanDataImpl(
        nameImpl = name,
        statusImpl = OtelJavaStatusData.create(status.statusCode.toOtelJavaStatusCode(), status.description),
        parentSpanContextImpl = parent.toOtelJavaSpanContext(),
        spanContextImpl = spanContext.toOtelJavaSpanContext(),
        kindImpl = spanKind.toOtelJavaSpanKind(),
        startEpochNanosImpl = startTimestamp,
        endEpochNanosImpl = endTimestamp ?: 0,
        resourceImpl = resource.let(::resourceFromMap),
        scopeImpl = OtelJavaInstrumentationLibraryInfo.create(
            instrumentationScopeInfo.name,
            instrumentationScopeInfo.version,
            instrumentationScopeInfo.schemaUrl
        ),
        attributesImpl = attrsFromMap(attributes),
        eventsImpl = events.map { it.toOtelJavaEventData() },
        linksImpl = links.map { it.toOtelJavaLinkData() },
        hasEndedImpl = hasEnded
    )
}
