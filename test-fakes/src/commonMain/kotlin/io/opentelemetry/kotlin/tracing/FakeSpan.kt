package io.opentelemetry.kotlin.tracing

import io.opentelemetry.kotlin.attributes.AttributesMutator
import io.opentelemetry.kotlin.attributes.FakeAttributesMutator
import io.opentelemetry.kotlin.tracing.data.FakeSpanLinkData
import io.opentelemetry.kotlin.tracing.data.SpanEventData
import io.opentelemetry.kotlin.tracing.data.SpanLinkData

class FakeSpan private constructor(
    name: String,
    override val spanContext: SpanContext,
    override val parent: SpanContext,
    private val attrs: FakeAttributesMutator,
) : Span, AttributesMutator by attrs {

    constructor(
        name: String = "",
        spanContext: SpanContext = FakeSpanContext.INVALID,
        parent: SpanContext = FakeSpanContext.INVALID,
    ) : this(name, spanContext, parent, FakeAttributesMutator())

    private var nameImpl: String = name
    val name: String get() = nameImpl

    private var statusImpl: StatusData = StatusData.Unset
    val status: StatusData get() = statusImpl

    val events: MutableList<SpanEventData> = mutableListOf()
    val links: MutableList<SpanLinkData> = mutableListOf()

    val attributes: Map<String, Any> get() = attrs.attributes

    private var recording: Boolean = true

    override fun setName(name: String) {
        nameImpl = name
    }

    override fun setStatus(status: StatusData) {
        statusImpl = status
    }

    override fun end() {
        recording = false
    }

    override fun end(timestamp: Long) {
        recording = false
    }

    override fun isRecording(): Boolean = recording

    override fun addLink(
        spanContext: SpanContext,
        attributes: (AttributesMutator.() -> Unit)?
    ) {
        val container = FakeAttributesMutator()
        if (attributes != null) {
            attributes(container)
        }
        val attrs = container.attributes
        links.add(FakeSpanLinkData(spanContext, attrs))
    }

    override fun addEvent(
        name: String,
        timestamp: Long?,
        attributes: (AttributesMutator.() -> Unit)?
    ) {
        val fakeSpanEvent = FakeSpanEvent(name, timestamp ?: 0)
        if (attributes != null) {
            attributes(fakeSpanEvent)
        }
        events.add(fakeSpanEvent)
    }
}
