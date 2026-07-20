package io.opentelemetry.kotlin.tracing

import io.opentelemetry.kotlin.attributes.AnyValue
import io.opentelemetry.kotlin.tracing.model.SpanEvent

class FakeSpanEvent(
    override val name: String,
    override val timestamp: Long
) : SpanEvent {

    override val attributes = mutableMapOf<String, Any>()

    override val droppedAttributesCount: Int = 0

    override fun setStringAttribute(key: String, value: String) {
        attributes[key] = value
    }

    override fun setBooleanAttribute(key: String, value: Boolean) {
        attributes[key] = value
    }

    override fun setLongAttribute(key: String, value: Long) {
        attributes[key] = value
    }

    override fun setDoubleAttribute(key: String, value: Double) {
        attributes[key] = value
    }

    override fun setBooleanListAttribute(key: String, value: List<Boolean>) {
        attributes[key] = value
    }

    override fun setStringListAttribute(key: String, value: List<String>) {
        attributes[key] = value
    }

    override fun setLongListAttribute(key: String, value: List<Long>) {
        attributes[key] = value
    }

    override fun setDoubleListAttribute(key: String, value: List<Double>) {
        attributes[key] = value
    }

    override fun setByteArrayAttribute(key: String, value: ByteArray) {
        attributes[key] = value
    }

    override fun setAnyValueAttribute(key: String, value: AnyValue) {
        attributes[key] = value
    }
}
