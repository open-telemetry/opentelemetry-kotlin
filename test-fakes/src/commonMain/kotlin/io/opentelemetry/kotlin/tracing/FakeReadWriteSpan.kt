package io.opentelemetry.kotlin.tracing

import io.opentelemetry.kotlin.FakeInstrumentationScopeInfo
import io.opentelemetry.kotlin.InstrumentationScopeInfo
import io.opentelemetry.kotlin.attributes.AttributesMutator
import io.opentelemetry.kotlin.attributes.FakeAttributesMutator
import io.opentelemetry.kotlin.resource.FakeResource
import io.opentelemetry.kotlin.resource.Resource
import io.opentelemetry.kotlin.tracing.data.SpanData
import io.opentelemetry.kotlin.tracing.data.SpanEventData
import io.opentelemetry.kotlin.tracing.data.SpanLinkData
import io.opentelemetry.kotlin.tracing.model.ReadWriteSpan

class FakeReadWriteSpan private constructor(
    name: String,
    status: StatusData,
    override val parent: SpanContext,
    spanContext: SpanContext,
    override val spanKind: SpanKind,
    override val startTimestamp: Long,
    override val events: List<SpanEventData>,
    override val droppedEventsCount: Int,
    override val links: List<SpanLinkData>,
    override val droppedLinksCount: Int,
    private val attrs: FakeAttributesMutator,
    override val endTimestamp: Long?,
    override val resource: Resource,
    override val instrumentationScopeInfo: InstrumentationScopeInfo,
    override val hasEnded: Boolean,
    override val droppedAttributesCount: Int,
) : ReadWriteSpan, AttributesMutator by attrs {

    constructor(
        name: String = "fake_span",
        status: StatusData = StatusData.Unset,
        parent: SpanContext = FakeSpanContext.INVALID,
        spanContext: SpanContext = FakeSpanContext.INVALID,
        spanKind: SpanKind = SpanKind.INTERNAL,
        startTimestamp: Long = 0,
        events: List<SpanEventData> = emptyList(),
        droppedEventsCount: Int = 0,
        links: List<SpanLinkData> = emptyList(),
        droppedLinksCount: Int = 0,
        attributes: Map<String, Any> = emptyMap(),
        endTimestamp: Long? = 0,
        resource: Resource = FakeResource(),
        instrumentationScopeInfo: InstrumentationScopeInfo = FakeInstrumentationScopeInfo(),
        hasEnded: Boolean = false,
        droppedAttributesCount: Int = 0
    ) : this(
        name,
        status,
        parent,
        spanContext,
        spanKind,
        startTimestamp,
        events,
        droppedEventsCount,
        links,
        droppedLinksCount,
        FakeAttributesMutator(attributes.toMutableMap()),
        endTimestamp,
        resource,
        instrumentationScopeInfo,
        hasEnded,
        droppedAttributesCount
    )

    override val attributes: Map<String, Any> get() = attrs.attributes

    override var spanContext: SpanContext = spanContext

    private var nameImpl: String = name
    override val name: String get() = nameImpl

    private var statusImpl: StatusData = status
    override val status: StatusData get() = statusImpl

    override fun setName(name: String) {
        nameImpl = name
    }

    override fun setStatus(status: StatusData) {
        statusImpl = status
    }

    override fun end() {
    }

    override fun end(timestamp: Long) {
    }

    override fun isRecording(): Boolean = true

    override fun addLink(
        spanContext: SpanContext,
        attributes: (AttributesMutator.() -> Unit)?
    ) {
        throw UnsupportedOperationException()
    }

    override fun addEvent(
        name: String,
        timestamp: Long?,
        attributes: (AttributesMutator.() -> Unit)?
    ) {
        throw UnsupportedOperationException()
    }

    override fun toSpanData(): SpanData {
        throw UnsupportedOperationException()
    }
}
