package io.opentelemetry.kotlin.tracing

import io.opentelemetry.kotlin.Clock
import io.opentelemetry.kotlin.attributes.MutableAttributeContainer
import io.opentelemetry.kotlin.export.DelegatingTelemetryCloseable
import io.opentelemetry.kotlin.export.TelemetryCloseable
import io.opentelemetry.kotlin.factory.ContextFactory
import io.opentelemetry.kotlin.factory.IdGenerator
import io.opentelemetry.kotlin.factory.SpanContextFactory
import io.opentelemetry.kotlin.factory.SpanFactory
import io.opentelemetry.kotlin.factory.TraceFlagsFactory
import io.opentelemetry.kotlin.factory.TraceStateFactory
import io.opentelemetry.kotlin.init.config.TracingConfig
import io.opentelemetry.kotlin.provider.ApiProviderImpl
import io.opentelemetry.kotlin.tracing.export.createCompositeSpanProcessor

internal class TracerProviderImpl(
    private val clock: Clock,
    tracingConfig: TracingConfig,
    contextFactory: ContextFactory,
    spanContextFactory: SpanContextFactory,
    traceFlagsFactory: TraceFlagsFactory,
    traceStateFactory: TraceStateFactory,
    spanFactory: SpanFactory,
    idGenerator: IdGenerator,
    private val closeable: DelegatingTelemetryCloseable = DelegatingTelemetryCloseable()
) : TracerProvider, TelemetryCloseable by closeable {

    private val apiProvider = ApiProviderImpl<Tracer> { key ->
        @Suppress("DEPRECATION")
        val processor = when {
            tracingConfig.processors.isEmpty() -> null
            else -> createCompositeSpanProcessor(
                tracingConfig.processors
            )
        }
        processor?.let(closeable::add)
        TracerImpl(
            clock = clock,
            processor = processor,
            contextFactory = contextFactory,
            spanContextFactory = spanContextFactory,
            traceFlagsFactory = traceFlagsFactory,
            traceStateFactory = traceStateFactory,
            spanFactory = spanFactory,
            tracingIdFactory = idGenerator,
            scope = key,
            resource = tracingConfig.resource,
            spanLimitConfig = tracingConfig.spanLimits
        )
    }

    override fun getTracer(
        name: String,
        version: String?,
        schemaUrl: String?,
        attributes: (MutableAttributeContainer.() -> Unit)?
    ): Tracer {
        val key = apiProvider.createInstrumentationScopeInfo(
            name = name,
            version = version,
            schemaUrl = schemaUrl,
            attributes = attributes
        )
        return apiProvider.getOrCreate(key)
    }
}
