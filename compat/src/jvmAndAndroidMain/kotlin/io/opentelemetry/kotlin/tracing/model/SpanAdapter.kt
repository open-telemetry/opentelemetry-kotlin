package io.opentelemetry.kotlin.tracing.model

import io.opentelemetry.kotlin.Clock
import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaAttributeKey
import io.opentelemetry.kotlin.aliases.OtelJavaContext
import io.opentelemetry.kotlin.aliases.OtelJavaImplicitContextKeyed
import io.opentelemetry.kotlin.aliases.OtelJavaScope
import io.opentelemetry.kotlin.aliases.OtelJavaSpan
import io.opentelemetry.kotlin.aliases.OtelJavaSpanContext
import io.opentelemetry.kotlin.attributes.CompatMutableAttributeContainer
import io.opentelemetry.kotlin.attributes.MutableAttributeContainer
import io.opentelemetry.kotlin.init.CompatSpanLimitsConfig
import io.opentelemetry.kotlin.tracing.LinkImpl
import io.opentelemetry.kotlin.tracing.SpanEventImpl
import io.opentelemetry.kotlin.tracing.data.EventData
import io.opentelemetry.kotlin.tracing.data.LinkData
import io.opentelemetry.kotlin.tracing.data.StatusData
import io.opentelemetry.kotlin.tracing.ext.toOtelJavaSpanContext
import io.opentelemetry.kotlin.tracing.ext.toOtelJavaStatusData
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalApi::class)
internal class SpanAdapter(
    val impl: OtelJavaSpan,
    private val clock: Clock,
    parentCtx: OtelJavaContext?,
    override val spanKind: SpanKind,
    override val startTimestamp: Long,
    private val spanLimitsConfig: CompatSpanLimitsConfig,
) : Span, OtelJavaImplicitContextKeyed {

    private val attrs: MutableMap<String, Any> = ConcurrentHashMap()
    private val eventsImpl: ConcurrentLinkedQueue<EventData> = ConcurrentLinkedQueue()
    private val linksImpl: ConcurrentLinkedQueue<Link> = ConcurrentLinkedQueue()

    private var implName: String = ""
    private var implStatus: StatusData = StatusData.Unset

    override val parent: SpanContext = SpanContextAdapter(
        parentCtx?.let { OtelJavaSpan.fromContext(it) }?.spanContext
            ?: OtelJavaSpanContext.getInvalid()
    )

    override var name: String
        get() = implName
        set(value) {
            implName = value
            impl.updateName(value)
        }

    override var status: StatusData
        get() = implStatus
        set(value) {
            implStatus = value
            value.toOtelJavaStatusData().let {
                impl.setStatus(it.statusCode, it.description)
            }
        }

    override val spanContext: SpanContext = SpanContextAdapter(impl.spanContext)

    override val attributes: Map<String, Any>
        get() = attrs.toMap()

    override val events: List<EventData>
        get() = eventsImpl.toList()

    override val links: List<LinkData>
        get() = linksImpl.toList()

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
        if (linksImpl.size < spanLimitsConfig.linkCountLimit) {
            linksImpl.add(LinkImpl(spanContext, container))
        }
        impl.addLink(spanContext.toOtelJavaSpanContext(), container.otelJavaAttributes())
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
        val time = timestamp ?: clock.now()
        if (eventsImpl.size < spanLimitsConfig.eventCountLimit) {
            eventsImpl.add(SpanEventImpl(name, time, container))
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

    override fun storeInContext(context: OtelJavaContext): OtelJavaContext? {
        return impl.storeInContext(context)
    }

    override fun makeCurrent(): OtelJavaScope? {
        return impl.makeCurrent()
    }
}
