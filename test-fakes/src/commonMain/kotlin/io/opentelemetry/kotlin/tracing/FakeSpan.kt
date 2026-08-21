package io.opentelemetry.kotlin.tracing

import io.opentelemetry.kotlin.attributes.AnyValue
import io.opentelemetry.kotlin.attributes.AttributesMutator
import io.opentelemetry.kotlin.attributes.FakeAttributesMutator
import io.opentelemetry.kotlin.tracing.data.FakeSpanLinkData
import io.opentelemetry.kotlin.tracing.data.SpanEventData
import io.opentelemetry.kotlin.tracing.data.SpanLinkData

class FakeSpan(
    name: String = "",
    override val spanContext: SpanContext = FakeSpanContext.INVALID,
    override val parent: SpanContext = FakeSpanContext.INVALID,
) : Span {

    private var nameImpl: String = name
    val name: String get() = nameImpl

    private var statusImpl: StatusData = StatusData.Unset
    val status: StatusData get() = statusImpl

    val events: MutableList<SpanEventData> = mutableListOf()
    val links: MutableList<SpanLinkData> = mutableListOf()

    private val attrs = FakeAttributesMutator()
    val attributes: Map<String, Any> get() = attrs.attributes

    private var recording: Boolean = true

    override fun setName(name: String) {
        nameImpl = name
    }

    override fun setStatus(status: StatusData) {
        statusImpl = status
    }

    override fun setBooleanAttribute(key: String, value: Boolean) {
        attrs.setBooleanAttribute(key, value)
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

    override fun setStringAttribute(key: String, value: String) {
        attrs.setStringAttribute(key, value)
    }

    override fun setLongAttribute(key: String, value: Long) {
        attrs.setLongAttribute(key, value)
    }

    override fun setDoubleAttribute(key: String, value: Double) {
        attrs.setDoubleAttribute(key, value)
    }

    override fun setBooleanListAttribute(key: String, value: List<Boolean>) {
        attrs.setBooleanListAttribute(key, value)
    }

    override fun setStringListAttribute(key: String, value: List<String>) {
        attrs.setStringListAttribute(key, value)
    }

    override fun setLongListAttribute(key: String, value: List<Long>) {
        attrs.setLongListAttribute(key, value)
    }

    override fun setDoubleListAttribute(key: String, value: List<Double>) {
        attrs.setDoubleListAttribute(key, value)
    }

    override fun setByteArrayAttribute(key: String, value: ByteArray) {
        attrs.setByteArrayAttribute(key, value)
    }

    override fun setAnyValueAttribute(key: String, value: AnyValue) {
        attrs.setAnyValueAttribute(key, value)
    }
}
