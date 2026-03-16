package io.opentelemetry.kotlin.tracing

import io.opentelemetry.kotlin.attributes.AttributesMutator
import io.opentelemetry.kotlin.attributes.FakeAttributesMutator
import io.opentelemetry.kotlin.exceptionType
import io.opentelemetry.kotlin.tracing.data.FakeSpanLinkData
import io.opentelemetry.kotlin.tracing.data.SpanEventData
import io.opentelemetry.kotlin.tracing.data.SpanLinkData
import io.opentelemetry.kotlin.tracing.data.StatusData
import io.opentelemetry.kotlin.tracing.model.Span
import io.opentelemetry.kotlin.tracing.model.SpanContext

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

    private var recording: Boolean = true

    override fun setName(name: String) {
        nameImpl = name
    }

    override fun setStatus(status: StatusData) {
        statusImpl = status
    }

    override fun setBooleanAttribute(key: String, value: Boolean) {
        TODO("Not yet implemented")
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

    override fun recordException(exception: Throwable, attributes: (AttributesMutator.() -> Unit)?) {
        addEvent("exception") {
            setStringAttribute("exception.stacktrace", exception.stackTraceToString())
            exception.message?.let { setStringAttribute("exception.message", it) }
            exception.exceptionType()?.let { setStringAttribute("exception.type", it) }
            attributes?.invoke(this)
        }
    }
}
