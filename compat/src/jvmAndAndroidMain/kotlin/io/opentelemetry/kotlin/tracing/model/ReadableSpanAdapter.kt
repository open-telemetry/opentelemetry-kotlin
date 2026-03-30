package io.opentelemetry.kotlin.tracing.model

import io.opentelemetry.kotlin.InstrumentationScopeInfo
import io.opentelemetry.kotlin.aliases.OtelJavaReadableSpan
import io.opentelemetry.kotlin.attributes.convertToMap
import io.opentelemetry.kotlin.resource.Resource
import io.opentelemetry.kotlin.resource.ResourceAdapter
import io.opentelemetry.kotlin.scope.toOtelKotlinInstrumentationScopeInfo
import io.opentelemetry.kotlin.tracing.SpanContext
import io.opentelemetry.kotlin.tracing.SpanKind
import io.opentelemetry.kotlin.tracing.StatusData
import io.opentelemetry.kotlin.tracing.data.SpanData
import io.opentelemetry.kotlin.tracing.data.SpanDataAdapter
import io.opentelemetry.kotlin.tracing.data.SpanEventData
import io.opentelemetry.kotlin.tracing.data.SpanEventDataAdapter
import io.opentelemetry.kotlin.tracing.data.SpanLinkData
import io.opentelemetry.kotlin.tracing.data.SpanLinkDataAdapter
import io.opentelemetry.kotlin.tracing.ext.toOtelKotlinSpanKind
import io.opentelemetry.kotlin.tracing.ext.toOtelKotlinStatusData

internal class ReadableSpanAdapter(
    val impl: OtelJavaReadableSpan
) : ReadableSpan {
    override val parent: SpanContext
    override val spanContext: SpanContext
    override val spanKind: SpanKind
    override val startTimestamp: Long
    override val resource: Resource
    override val instrumentationScopeInfo: InstrumentationScopeInfo

    override val name: String
        get() = impl.name
    override val status: StatusData
        get() = impl.toSpanData().status.toOtelKotlinStatusData()
    override val endTimestamp: Long
        get() = impl.toSpanData().endEpochNanos
    override val attributes: Map<String, Any>
        get() = impl.attributes.convertToMap()
    override val events: List<SpanEventData>
        get() = impl.toSpanData().events.map(::SpanEventDataAdapter)
    override val links: List<SpanLinkData>
        get() = impl.toSpanData().links.map(::SpanLinkDataAdapter)
    override val hasEnded: Boolean
        get() = impl.hasEnded()

    override fun toSpanData(): SpanData = SpanDataAdapter(impl.toSpanData())

    init {
        val initialState = impl.toSpanData()
        spanContext = SpanContextAdapter(impl.spanContext)
        parent = SpanContextAdapter(impl.parentSpanContext)
        spanKind = impl.kind.toOtelKotlinSpanKind()
        startTimestamp = initialState.startEpochNanos
        resource = ResourceAdapter(initialState.resource)
        instrumentationScopeInfo = impl.instrumentationScopeInfo.toOtelKotlinInstrumentationScopeInfo()
    }
}
