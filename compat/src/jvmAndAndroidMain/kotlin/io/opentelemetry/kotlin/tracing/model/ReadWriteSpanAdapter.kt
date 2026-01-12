package io.opentelemetry.kotlin.tracing.model

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaAttributeKey
import io.opentelemetry.kotlin.aliases.OtelJavaReadWriteSpan
import io.opentelemetry.kotlin.attributes.CompatMutableAttributeContainer
import io.opentelemetry.kotlin.attributes.MutableAttributeContainer
import io.opentelemetry.kotlin.tracing.data.StatusData
import io.opentelemetry.kotlin.tracing.ext.toOtelJavaStatusData
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalApi::class)
internal class ReadWriteSpanAdapter(
    val impl: OtelJavaReadWriteSpan,
    private val readableSpan: ReadableSpanAdapter = ReadableSpanAdapter(impl)
) : ReadWriteSpan, ReadableSpan by readableSpan {

    override var name: String
        get() = readableSpan.name
        set(value) {
            impl.updateName(value)
        }

    override var status: StatusData
        get() = readableSpan.status
        set(value) {
            val status = value.toOtelJavaStatusData()
            if (status.description.isEmpty()) {
                impl.setStatus(status.statusCode)
            } else {
                impl.setStatus(status.statusCode, status.description)
            }
        }

    override fun end() {
        impl.end()
    }

    override fun end(timestamp: Long) {
        impl.end(timestamp, TimeUnit.NANOSECONDS)
    }

    override fun isRecording(): Boolean = impl.isRecording

    override fun addLink(
        spanContext: SpanContext,
        attributes: (MutableAttributeContainer.() -> Unit)?
    ) {
        val container = CompatMutableAttributeContainer()
        if (attributes != null) {
            attributes(container)
        }
        val ctx = (spanContext as SpanContextAdapter).impl
        impl.addLink(ctx, container.otelJavaAttributes())
    }

    override fun addEvent(
        name: String,
        timestamp: Long?,
        attributes: (MutableAttributeContainer.() -> Unit)?
    ) {
        val container = CompatMutableAttributeContainer()
        if (attributes != null) {
            attributes(container)
        }
        impl.addEvent(name, container.otelJavaAttributes(), timestamp ?: 0, TimeUnit.NANOSECONDS)
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
}
