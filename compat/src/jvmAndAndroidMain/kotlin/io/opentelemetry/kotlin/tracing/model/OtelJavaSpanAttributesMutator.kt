package io.opentelemetry.kotlin.tracing.model

import io.opentelemetry.kotlin.aliases.OtelJavaAttributeKey
import io.opentelemetry.kotlin.aliases.OtelJavaSpan
import io.opentelemetry.kotlin.aliases.OtelJavaValue
import io.opentelemetry.kotlin.attributes.AnyValue
import io.opentelemetry.kotlin.attributes.AttributesMutator
import io.opentelemetry.kotlin.attributes.toOtelJavaValue

/**
 * Writes attributes directly to an [OtelJavaSpan], which applies limits and truncation. Lists are copied
 * because the Java SDK stores them by reference.
 */
internal class OtelJavaSpanAttributesMutator(private val span: OtelJavaSpan) : AttributesMutator {

    override fun setBooleanAttribute(key: String, value: Boolean) {
        span.setAttribute(key, value)
    }

    override fun setStringAttribute(key: String, value: String) {
        span.setAttribute(key, value)
    }

    override fun setLongAttribute(key: String, value: Long) {
        span.setAttribute(key, value)
    }

    override fun setDoubleAttribute(key: String, value: Double) {
        span.setAttribute(key, value)
    }

    override fun setBooleanListAttribute(key: String, value: List<Boolean>) {
        span.setAttribute(OtelJavaAttributeKey.booleanArrayKey(key), value.toList())
    }

    override fun setStringListAttribute(key: String, value: List<String>) {
        span.setAttribute(OtelJavaAttributeKey.stringArrayKey(key), value.toList())
    }

    override fun setLongListAttribute(key: String, value: List<Long>) {
        span.setAttribute(OtelJavaAttributeKey.longArrayKey(key), value.toList())
    }

    override fun setDoubleListAttribute(key: String, value: List<Double>) {
        span.setAttribute(OtelJavaAttributeKey.doubleArrayKey(key), value.toList())
    }

    override fun setByteArrayAttribute(key: String, value: ByteArray) {
        span.setAttribute(OtelJavaAttributeKey.valueKey(key), OtelJavaValue.of(value))
    }

    override fun setAnyValueAttribute(key: String, value: AnyValue) {
        span.setAttribute(OtelJavaAttributeKey.valueKey(key), value.toOtelJavaValue())
    }
}
