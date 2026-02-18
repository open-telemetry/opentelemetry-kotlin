package io.opentelemetry.kotlin.tracing

import io.opentelemetry.kotlin.Clock
import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.InstrumentationScopeInfo
import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.factory.SdkFactory
import io.opentelemetry.kotlin.init.config.SpanLimitConfig
import io.opentelemetry.kotlin.resource.Resource
import io.opentelemetry.kotlin.tracing.export.SpanProcessor
import io.opentelemetry.kotlin.tracing.model.CreatedSpan
import io.opentelemetry.kotlin.tracing.model.ReadWriteSpanImpl
import io.opentelemetry.kotlin.tracing.model.Span
import io.opentelemetry.kotlin.tracing.model.SpanContext
import io.opentelemetry.kotlin.tracing.model.SpanKind
import io.opentelemetry.kotlin.tracing.model.SpanModel
import io.opentelemetry.kotlin.tracing.model.SpanRelationships

@OptIn(ExperimentalApi::class)
internal class TracerImpl(
    private val clock: Clock,
    private val processor: SpanProcessor?,
    sdkFactory: SdkFactory,
    private val scope: InstrumentationScopeInfo,
    private val resource: Resource,
    private val spanLimitConfig: SpanLimitConfig,
) : Tracer {

    private val contextFactory = sdkFactory.contextFactory
    private val root = contextFactory.root()
    private val invalidSpanContext = sdkFactory.spanContextFactory.invalid
    private val traceFlagsDefault = sdkFactory.traceFlagsFactory.default
    private val traceStateDefault = sdkFactory.traceStateFactory.default
    private val spanFactory = sdkFactory.spanFactory
    private val tracingIdFactory = sdkFactory.tracingIdFactory

    @Suppress("DEPRECATION")
    @Deprecated(
        "Deprecated.",
        replaceWith = ReplaceWith("startSpan(name, parentContext, spanKind, startTimestamp, action)")
    )
    override fun createSpan(
        name: String,
        parentContext: Context?,
        spanKind: SpanKind,
        startTimestamp: Long?,
        action: (SpanRelationships.() -> Unit)?
    ): Span = startSpan(name, parentContext, spanKind, startTimestamp, action)

    override fun startSpan(
        name: String,
        parentContext: Context?,
        spanKind: SpanKind,
        startTimestamp: Long?,
        action: (SpanRelationships.() -> Unit)?
    ): Span {
        val ctx = parentContext ?: contextFactory.implicitContext()

        val parentSpanContext = when (ctx) {
            root -> invalidSpanContext
            else -> spanFactory.fromContext(ctx).spanContext
        }

        val spanContext = calculateSpanContext(parentSpanContext)

        val spanModel = SpanModel(
            clock = clock,
            processor = processor,
            name = name,
            spanKind = spanKind,
            startTimestamp = startTimestamp ?: clock.now(),
            instrumentationScopeInfo = scope,
            resource = resource,
            parent = parentSpanContext,
            spanContext = spanContext,
            spanLimitConfig = spanLimitConfig
        )
        if (action != null) {
            action(spanModel)
        }
        processor?.onStart(ReadWriteSpanImpl(spanModel), ctx)
        return CreatedSpan(spanModel)
    }

    private fun calculateSpanContext(parent: SpanContext): SpanContext {
        val factory = tracingIdFactory

        val traceId = if (parent.isValid) {
            parent.traceIdBytes
        } else {
            factory.generateTraceIdBytes()
        }
        val spanId = factory.generateSpanIdBytes()

        return SpanContextImpl(
            traceIdBytes = traceId,
            spanIdBytes = spanId,
            traceFlags = traceFlagsDefault,
            isValid = true,
            isRemote = false,
            traceState = traceStateDefault,
        )
    }
}
