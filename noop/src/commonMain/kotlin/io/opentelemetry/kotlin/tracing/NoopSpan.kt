package io.opentelemetry.kotlin.tracing

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.attributes.MutableAttributeContainer
import io.opentelemetry.kotlin.tracing.data.EventData
import io.opentelemetry.kotlin.tracing.data.LinkData
import io.opentelemetry.kotlin.tracing.data.StatusData
import io.opentelemetry.kotlin.tracing.model.Span
import io.opentelemetry.kotlin.tracing.model.SpanContext
import io.opentelemetry.kotlin.tracing.model.SpanKind

@ExperimentalApi
internal object NoopSpan : Span {

    override var name: String = ""
    override var status: StatusData = StatusData.Unset
    override val parent: SpanContext = NoopSpanContext
    override val spanContext: SpanContext = NoopSpanContext
    override val spanKind: SpanKind = SpanKind.INTERNAL
    override val startTimestamp: Long = -1L
    override val attributes: Map<String, Any> = emptyMap()
    override val events: List<EventData> = emptyList()
    override val links: List<LinkData> = emptyList()

    override fun end() {
    }

    override fun end(timestamp: Long) {
    }

    override fun addLink(spanContext: SpanContext, attributes: (MutableAttributeContainer.() -> Unit)?) {
    }

    override fun addEvent(name: String, timestamp: Long?, attributes: (MutableAttributeContainer.() -> Unit)?) {
    }

    override fun isRecording(): Boolean = false

    override fun setBooleanAttribute(key: String, value: Boolean) {
    }

    override fun setStringAttribute(key: String, value: String) {
    }

    override fun setLongAttribute(key: String, value: Long) {
    }

    override fun setDoubleAttribute(key: String, value: Double) {
    }

    override fun setBooleanListAttribute(key: String, value: List<Boolean>) {
    }

    override fun setStringListAttribute(key: String, value: List<String>) {
    }

    override fun setLongListAttribute(key: String, value: List<Long>) {
    }

    override fun setDoubleListAttribute(key: String, value: List<Double>) {
    }
}
