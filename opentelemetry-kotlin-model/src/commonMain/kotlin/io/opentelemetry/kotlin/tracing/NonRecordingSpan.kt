package io.opentelemetry.kotlin.tracing

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.attributes.MutableAttributeContainer
import io.opentelemetry.kotlin.tracing.data.EventData
import io.opentelemetry.kotlin.tracing.data.LinkData
import io.opentelemetry.kotlin.tracing.data.StatusData
import io.opentelemetry.kotlin.tracing.model.Span
import io.opentelemetry.kotlin.tracing.model.SpanContext
import io.opentelemetry.kotlin.tracing.model.SpanKind

/**
 * A reference to a [Span] that cannot actively record any data. This can be useful for
 * propagating traces where it's not necessary to mutate the span - e.g. if a caller only needs to
 * know the trace/span IDs for a parent span.
 */
@OptIn(ExperimentalApi::class)
class NonRecordingSpan(
    override val parent: SpanContext,
    override val spanContext: SpanContext,
) : Span {

    override var name: String = ""
    override var status: StatusData = StatusData.Unset

    override val spanKind: SpanKind
        get() = SpanKind.INTERNAL

    override val startTimestamp: Long
        get() = 0

    override val attributes: Map<String, Any>
        get() = emptyMap()

    override val events: List<EventData>
        get() = emptyList()

    override val links: List<LinkData>
        get() = emptyList()

    override fun setBooleanAttribute(key: String, value: Boolean) {
    }

    override fun end() {
    }

    override fun end(timestamp: Long) {
    }

    override fun isRecording(): Boolean = false

    override fun addLink(
        spanContext: SpanContext,
        attributes: (MutableAttributeContainer.() -> Unit)?
    ) {
    }

    override fun addEvent(
        name: String,
        timestamp: Long?,
        attributes: (MutableAttributeContainer.() -> Unit)?
    ) {
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
