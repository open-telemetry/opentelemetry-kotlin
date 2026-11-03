package io.opentelemetry.kotlin.tracing.model

import io.opentelemetry.kotlin.aliases.OtelJavaContext
import io.opentelemetry.kotlin.aliases.OtelJavaImplicitContextKeyed
import io.opentelemetry.kotlin.aliases.OtelJavaReadableSpan
import io.opentelemetry.kotlin.aliases.OtelJavaScope
import io.opentelemetry.kotlin.aliases.OtelJavaSpan
import io.opentelemetry.kotlin.attributes.AttributeContainer
import io.opentelemetry.kotlin.attributes.AttributesMutator
import io.opentelemetry.kotlin.attributes.CompatAttributesModel
import io.opentelemetry.kotlin.attributes.convertToMap
import io.opentelemetry.kotlin.error.SdkErrorHandler
import io.opentelemetry.kotlin.error.sdkGuard
import io.opentelemetry.kotlin.error.sdkGuardOrDefault
import io.opentelemetry.kotlin.error.userCode
import io.opentelemetry.kotlin.init.CompatSpanLimitsConfig
import io.opentelemetry.kotlin.tracing.Span
import io.opentelemetry.kotlin.tracing.SpanContext
import io.opentelemetry.kotlin.tracing.SpanCreationAction
import io.opentelemetry.kotlin.tracing.SpanKind
import io.opentelemetry.kotlin.tracing.SpanLinkCompatImpl
import io.opentelemetry.kotlin.tracing.StatusData
import io.opentelemetry.kotlin.tracing.createInvalidSpanContext
import io.opentelemetry.kotlin.tracing.data.SpanLinkData
import io.opentelemetry.kotlin.tracing.ext.toOtelJavaSpanContext
import io.opentelemetry.kotlin.tracing.ext.toOtelJavaStatusData
import io.opentelemetry.kotlin.tracing.ext.toOtelKotlinSpanContext
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.TimeUnit

internal class SpanAdapter(
    val impl: OtelJavaSpan,
    parentCtx: OtelJavaContext?,
    val spanKind: SpanKind,
    private val spanLimitsConfig: CompatSpanLimitsConfig,
    private val sdkErrorHandler: SdkErrorHandler,
    creationState: CompatSpanCreationState? = null,
) : Span,
    AttributeContainer,
    SpanCreationAction,
    OtelJavaImplicitContextKeyed,
    AttributesMutator by OtelJavaSpanAttributesMutator(impl) {

    private val linksImpl: ConcurrentLinkedQueue<SpanLink> =
        ConcurrentLinkedQueue(creationState?.links.orEmpty())

    override val parent: SpanContext =
        parentCtx?.let { OtelJavaSpan.fromContext(it) }?.spanContext?.toOtelKotlinSpanContext()
            ?: createInvalidSpanContext()

    override val spanContext: SpanContext = impl.spanContext.toOtelKotlinSpanContext()

    override val attributes: Map<String, Any>
        get() = sdkErrorHandler.sdkGuardOrDefault(emptyMap(), "Span.attributes failed") {
            (impl as? OtelJavaReadableSpan)?.attributes?.convertToMap().orEmpty()
        }

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
        sdkErrorHandler.sdkGuard("Span.end failed") {
            impl.end()
        }
    }

    override fun end(timestamp: Long) {
        sdkErrorHandler.sdkGuard("Span.end failed") {
            if (timestamp > 0) {
                impl.end(timestamp, TimeUnit.NANOSECONDS)
            } else {
                impl.end()
            }
        }
    }

    override fun isRecording(): Boolean = impl.isRecording

    override fun addLink(
        spanContext: SpanContext,
        attributes: (AttributesMutator.() -> Unit)?
    ) = sdkErrorHandler.sdkGuard("Span.addLink failed") {
        val container = CompatAttributesModel()
        if (attributes != null) {
            userCode { attributes(container) }
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
    ) = sdkErrorHandler.sdkGuard("Span.addEvent failed") {
        val container = CompatAttributesModel()
        if (attributes != null) {
            userCode { attributes(container) }
        }
        // As with the span start: left unset, the SDK stamps the event with the clock it times the span by.
        if (timestamp != null && timestamp > 0) {
            impl.addEvent(name, container.otelJavaAttributes(), timestamp, TimeUnit.NANOSECONDS)
        } else {
            impl.addEvent(name, container.otelJavaAttributes())
        }
    }

    override fun storeInContext(context: OtelJavaContext): OtelJavaContext? {
        return impl.storeInContext(context)
    }

    override fun makeCurrent(): OtelJavaScope? {
        return impl.makeCurrent()
    }
}
