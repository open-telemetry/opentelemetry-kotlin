package io.opentelemetry.kotlin.tracing.model

import io.opentelemetry.kotlin.aliases.OtelJavaAttributeKey
import io.opentelemetry.kotlin.aliases.OtelJavaReadWriteSpan
import io.opentelemetry.kotlin.aliases.OtelJavaValue
import io.opentelemetry.kotlin.attributes.AnyValue
import io.opentelemetry.kotlin.attributes.AttributesMutator
import io.opentelemetry.kotlin.attributes.CompatAttributesModel
import io.opentelemetry.kotlin.attributes.toOtelJavaValue
import io.opentelemetry.kotlin.tracing.SpanContext
import io.opentelemetry.kotlin.tracing.StatusData
import io.opentelemetry.kotlin.tracing.ext.toOtelJavaSpanContext
import io.opentelemetry.kotlin.tracing.ext.toOtelJavaStatusData
import java.util.concurrent.TimeUnit

internal class ReadWriteSpanAdapter(
    val impl: OtelJavaReadWriteSpan,
    private val readableSpan: ReadableSpanAdapter = ReadableSpanAdapter(impl)
) : ReadWriteSpan, ReadableSpan by readableSpan {

    override var spanContext: SpanContext = readableSpan.spanContext

    override fun setName(name: String) {
        impl.updateName(name)
    }

    override fun setStatus(status: StatusData) {
        val javaStatus = status.toOtelJavaStatusData()
        if (javaStatus.description.isEmpty()) {
            impl.setStatus(javaStatus.statusCode)
        } else {
            impl.setStatus(javaStatus.statusCode, javaStatus.description)
        }
    }

    override fun end() {
        impl.end()
    }

    override fun end(timestamp: Long) {
        if (timestamp > 0) {
            impl.end(timestamp, TimeUnit.NANOSECONDS)
        } else {
            impl.end()
        }
    }

    override fun isRecording(): Boolean = impl.isRecording

    override fun addLink(
        spanContext: SpanContext,
        attributes: (AttributesMutator.() -> Unit)?
    ) {
        val container = CompatAttributesModel()
        if (attributes != null) {
            attributes(container)
        }
        val ctx = spanContext.toOtelJavaSpanContext()
        impl.addLink(ctx, container.otelJavaAttributes())
    }

    override fun addEvent(
        name: String,
        timestamp: Long?,
        attributes: (AttributesMutator.() -> Unit)?
    ) {
        val container = CompatAttributesModel()
        if (attributes != null) {
            attributes(container)
        }
        if (timestamp != null && timestamp > 0) {
            impl.addEvent(name, container.otelJavaAttributes(), timestamp, TimeUnit.NANOSECONDS)
        } else {
            impl.addEvent(name, container.otelJavaAttributes())
        }
    }

    override fun setBooleanAttribute(key: String, value: Boolean) {
        impl.setAttribute(key, value)
    }

    override fun setStringAttribute(key: String, value: String) {
        impl.setAttribute(key, value)
    }

    override fun setLongAttribute(key: String, value: Long) {
        impl.setAttribute(key, value)
    }

    override fun setDoubleAttribute(key: String, value: Double) {
        impl.setAttribute(key, value)
    }

    override fun setBooleanListAttribute(key: String, value: List<Boolean>) {
        impl.setAttribute(OtelJavaAttributeKey.booleanArrayKey(key), value)
    }

    override fun setStringListAttribute(key: String, value: List<String>) {
        impl.setAttribute(OtelJavaAttributeKey.stringArrayKey(key), value)
    }

    override fun setLongListAttribute(key: String, value: List<Long>) {
        impl.setAttribute(OtelJavaAttributeKey.longArrayKey(key), value)
    }

    override fun setDoubleListAttribute(key: String, value: List<Double>) {
        impl.setAttribute(OtelJavaAttributeKey.doubleArrayKey(key), value)
    }

    override fun setByteArrayAttribute(key: String, value: ByteArray) {
        impl.setAttribute(OtelJavaAttributeKey.valueKey(key), OtelJavaValue.of(value))
    }

    override fun setAnyValueAttribute(key: String, value: AnyValue) {
        impl.setAttribute(OtelJavaAttributeKey.valueKey(key), value.toOtelJavaValue())
    }
}
