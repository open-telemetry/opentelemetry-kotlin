package io.opentelemetry.kotlin.tracing

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.InstrumentationScopeInfo
import io.opentelemetry.kotlin.resource.MutableResource
import io.opentelemetry.kotlin.resource.Resource
import io.opentelemetry.kotlin.tracing.data.SpanData
import io.opentelemetry.kotlin.tracing.data.SpanEventData
import io.opentelemetry.kotlin.tracing.data.SpanLinkData
import io.opentelemetry.kotlin.tracing.model.ReadableSpan

@OptIn(ExperimentalApi::class)
internal object NoopReadableSpan : ReadableSpan {

    override val name: String = ""
    override val status: StatusData = StatusData.Unset
    override val parent: SpanContext = createInvalidSpanContext()
    override val spanContext: SpanContext = createInvalidSpanContext()
    override val spanKind: SpanKind = SpanKind.INTERNAL
    override val startTimestamp: Long = 0
    override val endTimestamp: Long? = null
    override val events: List<SpanEventData> = emptyList()
    override val droppedEventsCount: Int = 0
    override val links: List<SpanLinkData> = emptyList()
    override val droppedLinksCount: Int = 0
    override val attributes: Map<String, Any> = emptyMap()
    override val droppedAttributesCount: Int = 0
    override val hasEnded: Boolean = true

    override val resource: Resource = object : Resource {
        override val attributes: Map<String, Any> = emptyMap()
        override val schemaUrl: String? = null
        override fun asNewResource(action: MutableResource.() -> Unit): Resource = this
        override fun merge(other: Resource): Resource = this
    }

    override val instrumentationScopeInfo: InstrumentationScopeInfo = object : InstrumentationScopeInfo {
        override val name: String = ""
        override val version: String? = null
        override val schemaUrl: String? = null
        override val attributes: Map<String, Any> = emptyMap()
    }

    override fun toSpanData(): SpanData = this
}
