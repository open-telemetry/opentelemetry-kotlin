package io.opentelemetry.kotlin.tracing.model

import io.opentelemetry.kotlin.aliases.OtelJavaAttributeKey
import io.opentelemetry.kotlin.aliases.OtelJavaContext
import io.opentelemetry.kotlin.aliases.OtelJavaImplicitContextKeyed
import io.opentelemetry.kotlin.aliases.OtelJavaScope
import io.opentelemetry.kotlin.aliases.OtelJavaSpan
import io.opentelemetry.kotlin.attributes.AnyValue
import io.opentelemetry.kotlin.attributes.AttributeContainer
import io.opentelemetry.kotlin.attributes.AttributesMutator
import io.opentelemetry.kotlin.attributes.CompatAttributesModel
import io.opentelemetry.kotlin.attributes.setFlattenedAnyValueAttribute
import io.opentelemetry.kotlin.factory.DefaultSpanContextFactory
import io.opentelemetry.kotlin.init.CompatSpanLimitsConfig
import io.opentelemetry.kotlin.tracing.Span
import io.opentelemetry.kotlin.tracing.SpanContext
import io.opentelemetry.kotlin.tracing.SpanCreationAction
import io.opentelemetry.kotlin.tracing.SpanKind
import io.opentelemetry.kotlin.tracing.SpanLinkCompatImpl
import io.opentelemetry.kotlin.tracing.StatusData
import io.opentelemetry.kotlin.tracing.data.SpanLinkData
import io.opentelemetry.kotlin.tracing.ext.toOtelJavaSpanContext
import io.opentelemetry.kotlin.tracing.ext.toOtelJavaStatusData
import io.opentelemetry.kotlin.tracing.ext.toOtelKotlinSpanContext
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.TimeUnit

internal class SpanAdapter(
    val impl: OtelJavaSpan,
    parentCtx: OtelJavaContext?,
    val spanKind: SpanKind,
    private val spanLimitsConfig: CompatSpanLimitsConfig,
    creationState: CompatSpanCreationState? = null,
) : Span, AttributeContainer, SpanCreationAction, OtelJavaImplicitContextKeyed {

    private val attrs: MutableMap<String, Any> = ConcurrentHashMap(creationState?.attributes.orEmpty())
    private val linksImpl: ConcurrentLinkedQueue<SpanLink> =
        ConcurrentLinkedQueue(creationState?.links.orEmpty())

    override val parent: SpanContext =
        parentCtx?.let { OtelJavaSpan.fromContext(it) }?.spanContext?.toOtelKotlinSpanContext()
            ?: DefaultSpanContextFactory.invalid

    override val spanContext: SpanContext = impl.spanContext.toOtelKotlinSpanContext()

    override val attributes: Map<String, Any>
        get() = attrs.toMap()

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
        if (linksImpl.size < spanLimitsConfig.effectiveLinkCountLimit) {
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
        // As with the span start: left unset, the SDK stamps the event with the clock it times the span by.
        if (timestamp != null && timestamp > 0) {
            impl.addEvent(name, container.otelJavaAttributes(), timestamp, TimeUnit.NANOSECONDS)
        } else {
            impl.addEvent(name, container.otelJavaAttributes())
        }
    }

    override fun setBooleanAttribute(key: String, value: Boolean) {
        impl.setAttribute(key, value)
        if (attrs.size < spanLimitsConfig.effectiveAttributeCountLimit) {
            attrs[key] = value
        }
    }

    override fun setStringAttribute(key: String, value: String) {
        impl.setAttribute(key, value)
        if (attrs.size < spanLimitsConfig.effectiveAttributeCountLimit) {
            attrs[key] = value
        }
    }

    override fun setLongAttribute(key: String, value: Long) {
        impl.setAttribute(key, value)
        if (attrs.size < spanLimitsConfig.effectiveAttributeCountLimit) {
            attrs[key] = value
        }
    }

    override fun setDoubleAttribute(key: String, value: Double) {
        impl.setAttribute(key, value)
        if (attrs.size < spanLimitsConfig.effectiveAttributeCountLimit) {
            attrs[key] = value
        }
    }

    override fun setBooleanListAttribute(key: String, value: List<Boolean>) {
        impl.setAttribute(OtelJavaAttributeKey.booleanArrayKey(key), value)
        if (attrs.size < spanLimitsConfig.effectiveAttributeCountLimit) {
            attrs[key] = value
        }
    }

    override fun setStringListAttribute(key: String, value: List<String>) {
        impl.setAttribute(OtelJavaAttributeKey.stringArrayKey(key), value)
        if (attrs.size < spanLimitsConfig.effectiveAttributeCountLimit) {
            attrs[key] = value
        }
    }

    override fun setLongListAttribute(key: String, value: List<Long>) {
        impl.setAttribute(OtelJavaAttributeKey.longArrayKey(key), value)
        if (attrs.size < spanLimitsConfig.effectiveAttributeCountLimit) {
            attrs[key] = value
        }
    }

    override fun setDoubleListAttribute(key: String, value: List<Double>) {
        impl.setAttribute(OtelJavaAttributeKey.doubleArrayKey(key), value)
        if (attrs.size < spanLimitsConfig.effectiveAttributeCountLimit) {
            attrs[key] = value
        }
    }

    override fun setByteArrayAttribute(key: String, value: ByteArray) {
        // no java implementation available
    }

    override fun setAnyValueAttribute(key: String, value: AnyValue) {
        setFlattenedAnyValueAttribute(key, value)
    }

    override fun storeInContext(context: OtelJavaContext): OtelJavaContext? {
        return impl.storeInContext(context)
    }

    override fun makeCurrent(): OtelJavaScope? {
        return impl.makeCurrent()
    }
}
