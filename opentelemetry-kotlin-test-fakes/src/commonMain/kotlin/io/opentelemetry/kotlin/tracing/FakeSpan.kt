package io.opentelemetry.kotlin.tracing

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.attributes.FakeMutableAttributeContainer
import io.opentelemetry.kotlin.attributes.MutableAttributeContainer
import io.opentelemetry.kotlin.tracing.data.EventData
import io.opentelemetry.kotlin.tracing.data.FakeLinkData
import io.opentelemetry.kotlin.tracing.data.LinkData
import io.opentelemetry.kotlin.tracing.data.StatusData
import io.opentelemetry.kotlin.tracing.model.Span
import io.opentelemetry.kotlin.tracing.model.SpanContext
import io.opentelemetry.kotlin.tracing.model.SpanKind

@Suppress("UNUSED_PARAMETER")
@OptIn(ExperimentalApi::class)
class FakeSpan(
    override var name: String = "",
    override val spanContext: SpanContext = FakeSpanContext.INVALID,
) : Span {

    override val events: MutableList<EventData> = mutableListOf()
    override val links: MutableList<LinkData> = mutableListOf()

    override fun setBooleanAttribute(key: String, value: Boolean) {
        TODO("Not yet implemented")
    }

    override var status: StatusData
        get() = TODO("Not yet implemented")
        set(value) {}
    override val parent: SpanContext = FakeSpanContext.INVALID
    override val spanKind: SpanKind
        get() = TODO("Not yet implemented")
    override val startTimestamp: Long
        get() = TODO("Not yet implemented")

    override fun end() {
        TODO("Not yet implemented")
    }

    override fun end(timestamp: Long) {
        TODO("Not yet implemented")
    }

    override fun isRecording(): Boolean {
        TODO("Not yet implemented")
    }

    override fun addLink(
        spanContext: SpanContext,
        attributes: (MutableAttributeContainer.() -> Unit)?
    ) {
        val container = FakeMutableAttributeContainer()
        if (attributes != null) {
            attributes(container)
        }
        val attrs = container.attributes
        links.add(FakeLinkData(spanContext, attrs))
    }

    override fun addEvent(
        name: String,
        timestamp: Long?,
        attributes: (MutableAttributeContainer.() -> Unit)?
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
