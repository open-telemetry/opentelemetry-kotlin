package io.opentelemetry.kotlin.tracing.model

import io.opentelemetry.kotlin.aliases.OtelJavaReadWriteSpan
import io.opentelemetry.kotlin.attributes.AttributesMutator
import io.opentelemetry.kotlin.attributes.OtelJavaAttributesMutator
import io.opentelemetry.kotlin.attributes.attributeSink
import io.opentelemetry.kotlin.attributes.compatAttributes
import io.opentelemetry.kotlin.error.SdkErrorHandler
import io.opentelemetry.kotlin.error.sdkGuard
import io.opentelemetry.kotlin.tracing.SpanContext
import io.opentelemetry.kotlin.tracing.StatusData
import io.opentelemetry.kotlin.tracing.ext.toOtelJavaSpanContext
import io.opentelemetry.kotlin.tracing.ext.toOtelJavaStatusData
import java.util.concurrent.TimeUnit

internal class ReadWriteSpanAdapter(
    val impl: OtelJavaReadWriteSpan,
    private val sdkErrorHandler: SdkErrorHandler,
    private val readableSpan: ReadableSpanAdapter = ReadableSpanAdapter(impl)
) : ReadWriteSpan,
    ReadableSpan by readableSpan,
    AttributesMutator by OtelJavaAttributesMutator(impl.attributeSink()) {

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
    ) = sdkErrorHandler.sdkGuard("Span.addLink failed") {
        val container = compatAttributes(attributes)
        impl.addLink(spanContext.toOtelJavaSpanContext(), container.otelJavaAttributes())
    }

    override fun addEvent(
        name: String,
        timestamp: Long?,
        attributes: (AttributesMutator.() -> Unit)?
    ) = sdkErrorHandler.sdkGuard("Span.addEvent failed") {
        val container = compatAttributes(attributes)
        if (timestamp != null && timestamp > 0) {
            impl.addEvent(name, container.otelJavaAttributes(), timestamp, TimeUnit.NANOSECONDS)
        } else {
            impl.addEvent(name, container.otelJavaAttributes())
        }
    }
}
