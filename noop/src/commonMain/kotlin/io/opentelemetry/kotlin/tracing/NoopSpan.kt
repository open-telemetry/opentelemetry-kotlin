package io.opentelemetry.kotlin.tracing

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.attributes.AttributesMutator
import io.opentelemetry.kotlin.tracing.data.StatusData
import io.opentelemetry.kotlin.tracing.model.Span
import io.opentelemetry.kotlin.tracing.model.SpanContext

@ExperimentalApi
internal object NoopSpan : Span {

    override val spanContext: SpanContext = NoopSpanContext
    override val parent: SpanContext = NoopSpanContext

    override fun setName(name: String) {
    }

    override fun setStatus(status: StatusData) {
    }

    override fun end() {
    }

    override fun end(timestamp: Long) {
    }

    override fun addLink(spanContext: SpanContext, attributes: (AttributesMutator.() -> Unit)?) {
    }

    override fun addEvent(name: String, timestamp: Long?, attributes: (AttributesMutator.() -> Unit)?) {
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

    override fun recordException(exception: Throwable, attributes: (AttributesMutator.() -> Unit)?) {
    }
}
