package io.opentelemetry.kotlin.tracing

import io.opentelemetry.kotlin.attributes.AttributesMutator
import io.opentelemetry.kotlin.attributes.FakeAttributesMutator
import io.opentelemetry.kotlin.tracing.data.FakeSpanLinkData
import io.opentelemetry.kotlin.tracing.data.SpanEventData
import io.opentelemetry.kotlin.tracing.data.SpanLinkData
import io.opentelemetry.kotlin.tracing.data.StatusData
import io.opentelemetry.kotlin.tracing.model.Span
import io.opentelemetry.kotlin.tracing.model.SpanContext
import io.opentelemetry.kotlin.tracing.model.SpanKind

@Suppress("UNUSED_PARAMETER")
class FakeSpan(
    override var name: String = "",
    override val spanContext: SpanContext = FakeSpanContext.INVALID,
) : Span {

    override val events: MutableList<SpanEventData> = mutableListOf()
    override val links: MutableList<SpanLinkData> = mutableListOf()

    private var recording: Boolean = true

    override fun setBooleanAttribute(key: String, value: Boolean) {
        TODO("Not yet implemented")
    }

    override var status: StatusData = StatusData.Unset
    override val parent: SpanContext = FakeSpanContext.INVALID
    override val spanKind: SpanKind
        get() = TODO("Not yet implemented")
    override val startTimestamp: Long
        get() = TODO("Not yet implemented")

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

    override fun setStringAttribute(key: String, value: String) {
        TODO("Not yet implemented")
    }

    override fun setLongAttribute(key: String, value: Long) {
        TODO("Not yet implemented")
    }

    override fun setDoubleAttribute(key: String, value: Double) {
        TODO("Not yet implemented")
    }

    override fun setBooleanListAttribute(key: String, value: List<Boolean>) {
        TODO("Not yet implemented")
    }

    override fun setStringListAttribute(key: String, value: List<String>) {
        TODO("Not yet implemented")
    }

    override fun setLongListAttribute(key: String, value: List<Long>) {
        TODO("Not yet implemented")
    }

    override fun setDoubleListAttribute(key: String, value: List<Double>) {
        TODO("Not yet implemented")
    }

    override val attributes: Map<String, Any>
        get() = TODO("Not yet implemented")
}
