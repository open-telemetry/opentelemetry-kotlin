package io.opentelemetry.kotlin.tracing.model

import io.opentelemetry.kotlin.Clock
import io.opentelemetry.kotlin.aliases.OtelJavaAttributeKey
import io.opentelemetry.kotlin.aliases.OtelJavaContext
import io.opentelemetry.kotlin.aliases.OtelJavaImplicitContextKeyed
import io.opentelemetry.kotlin.aliases.OtelJavaScope
import io.opentelemetry.kotlin.aliases.OtelJavaSpan
import io.opentelemetry.kotlin.aliases.OtelJavaSpanContext
import io.opentelemetry.kotlin.attributes.AttributeContainer
import io.opentelemetry.kotlin.attributes.AttributesMutator
import io.opentelemetry.kotlin.attributes.CompatAttributesModel
import io.opentelemetry.kotlin.init.CompatSpanLimitsConfig
import io.opentelemetry.kotlin.tracing.SpanEventCompatImpl
import io.opentelemetry.kotlin.tracing.SpanLinkCompatImpl
import io.opentelemetry.kotlin.tracing.data.SpanEventData
import io.opentelemetry.kotlin.tracing.data.SpanLinkData
import io.opentelemetry.kotlin.tracing.data.StatusData
import io.opentelemetry.kotlin.tracing.ext.toOtelJavaSpanContext
import io.opentelemetry.kotlin.tracing.ext.toOtelJavaStatusData
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.TimeUnit

internal class SpanAdapter(
    val impl: OtelJavaSpan,
    private val clock: Clock,
    parentCtx: OtelJavaContext?,
    val spanKind: SpanKind,
    val startTimestamp: Long,
    private val spanLimitsConfig: CompatSpanLimitsConfig,
) : Span, AttributeContainer, SpanCreationAction, OtelJavaImplicitContextKeyed {

    private val attrs: MutableMap<String, Any> = ConcurrentHashMap()
    private val eventsImpl: ConcurrentLinkedQueue<SpanEventData> = ConcurrentLinkedQueue()
    private val linksImpl: ConcurrentLinkedQueue<SpanLink> = ConcurrentLinkedQueue()

    override val parent: SpanContext = SpanContextAdapter(
        parentCtx?.let { OtelJavaSpan.fromContext(it) }?.spanContext
            ?: OtelJavaSpanContext.getInvalid()
    )

    override val spanContext: SpanContext = SpanContextAdapter(impl.spanContext)

    override val attributes: Map<String, Any>
        get() = attrs.toMap()

    val events: List<SpanEventData>
        get() = eventsImpl.toList()

    val links: List<SpanLinkData>
        get() = linksImpl.toList()

    override fun setName(name: String) {
        impl.updateName(name)
    }

    override fun setStatus(status: StatusData) {
        status.toOtelJavaStatusData().let {
            impl.setStatus(it.statusCode, it.description)
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
        attributes: (AttributesMutator.() -> Unit)?
    ) {
        val container = CompatAttributesModel()
        if (attributes != null) {
            attributes(container)
        }
        if (linksImpl.size < spanLimitsConfig.linkCountLimit) {
            linksImpl.add(SpanLinkCompatImpl(spanContext, container))
        }
        impl.addLink(spanContext.toOtelJavaSpanContext(), container.otelJavaAttributes())
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
        val time = timestamp ?: clock.now()
        if (eventsImpl.size < spanLimitsConfig.eventCountLimit) {
            eventsImpl.add(SpanEventCompatImpl(name, time, container))
        }
        impl.addEvent(name, container.otelJavaAttributes(), time, TimeUnit.NANOSECONDS)
    }

    override fun setBooleanAttribute(key: String, value: Boolean) {
        impl.setAttribute(key, value)
        if (attrs.size < spanLimitsConfig.attributeCountLimit) {
            attrs[key] = value
        }
    }

    override fun setStringAttribute(key: String, value: String) {
        impl.setAttribute(key, value)
        if (attrs.size < spanLimitsConfig.attributeCountLimit) {
            attrs[key] = value
        }
    }

    override fun setLongAttribute(key: String, value: Long) {
        impl.setAttribute(key, value)
        if (attrs.size < spanLimitsConfig.attributeCountLimit) {
            attrs[key] = value
        }
    }

    override fun setDoubleAttribute(key: String, value: Double) {
        impl.setAttribute(key, value)
        if (attrs.size < spanLimitsConfig.attributeCountLimit) {
            attrs[key] = value
        }
    }

    override fun setBooleanListAttribute(key: String, value: List<Boolean>) {
        impl.setAttribute(OtelJavaAttributeKey.booleanArrayKey(key), value)
        if (attrs.size < spanLimitsConfig.attributeCountLimit) {
            attrs[key] = value
        }
    }

    override fun setStringListAttribute(key: String, value: List<String>) {
        impl.setAttribute(OtelJavaAttributeKey.stringArrayKey(key), value)
        if (attrs.size < spanLimitsConfig.attributeCountLimit) {
            attrs[key] = value
        }
    }

    override fun setLongListAttribute(key: String, value: List<Long>) {
        impl.setAttribute(OtelJavaAttributeKey.longArrayKey(key), value)
        if (attrs.size < spanLimitsConfig.attributeCountLimit) {
            attrs[key] = value
        }
    }

    override fun setDoubleListAttribute(key: String, value: List<Double>) {
        impl.setAttribute(OtelJavaAttributeKey.doubleArrayKey(key), value)
        if (attrs.size < spanLimitsConfig.attributeCountLimit) {
            attrs[key] = value
        }
    }

    override fun recordException(exception: Throwable, attributes: (AttributesMutator.() -> Unit)?) {
        val container = CompatAttributesModel()
        if (attributes != null) {
            attributes(container)
        }
        impl.recordException(exception, container.otelJavaAttributes())
    }

    override fun storeInContext(context: OtelJavaContext): OtelJavaContext? {
        return impl.storeInContext(context)
    }

    override fun makeCurrent(): OtelJavaScope? {
        return impl.makeCurrent()
    }
}
