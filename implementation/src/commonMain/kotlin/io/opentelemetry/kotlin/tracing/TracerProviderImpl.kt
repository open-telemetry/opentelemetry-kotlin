package io.opentelemetry.kotlin.tracing

import io.opentelemetry.kotlin.Clock
import io.opentelemetry.kotlin.NoopOpenTelemetry
import io.opentelemetry.kotlin.attributes.AttributesMutator
import io.opentelemetry.kotlin.export.BatchTelemetryDefaults
import io.opentelemetry.kotlin.export.CompositeTelemetryCloseable
import io.opentelemetry.kotlin.export.MutableShutdownState
import io.opentelemetry.kotlin.export.OperationResultCode
import io.opentelemetry.kotlin.export.TelemetryCloseable
import io.opentelemetry.kotlin.export.runWithTimeout
import io.opentelemetry.kotlin.factory.ContextFactory
import io.opentelemetry.kotlin.factory.IdGenerator
import io.opentelemetry.kotlin.factory.SpanContextFactory
import io.opentelemetry.kotlin.factory.SpanFactory
import io.opentelemetry.kotlin.factory.TraceFlagsFactory
import io.opentelemetry.kotlin.init.config.TracingConfig
import io.opentelemetry.kotlin.provider.ApiProviderImpl

internal class TracerProviderImpl(
    private val clock: Clock,
    tracingConfig: TracingConfig,
    contextFactory: ContextFactory,
    spanContextFactory: SpanContextFactory,
    traceFlagsFactory: TraceFlagsFactory,
    spanFactory: SpanFactory,
    private val idGenerator: IdGenerator,
) : TracerProvider, TelemetryCloseable {

    private val shutdownState: MutableShutdownState = MutableShutdownState()
    private val closeable: TelemetryCloseable = CompositeTelemetryCloseable(
        tracingConfig.processor?.let { listOf(it) } ?: emptyList()
    )
    private val noopTracer = NoopOpenTelemetry.tracerProvider.getTracer("")

    private val sampler = tracingConfig.samplerFactory(spanFactory)

    private val apiProvider = ApiProviderImpl<Tracer> { key ->
        TracerImpl(
            clock = clock,
            processor = tracingConfig.processor,
            contextFactory = contextFactory,
            spanContextFactory = spanContextFactory,
            traceFlagsFactory = traceFlagsFactory,
            scope = key,
            resource = tracingConfig.resource,
            spanLimitConfig = tracingConfig.spanLimits,
            idGenerator = idGenerator,
            shutdownState = shutdownState,
            sampler = sampler,
        )
    }

    override fun getTracer(
        name: String,
        version: String?,
        schemaUrl: String?,
        attributes: (AttributesMutator.() -> Unit)?
    ): Tracer =
        shutdownState.ifActiveOrElse(noopTracer) {
            val key = apiProvider.createInstrumentationScopeInfo(
                name = name,
                version = version,
                schemaUrl = schemaUrl,
                attributes = attributes
            )
            apiProvider.getOrCreate(key)
        }

    override suspend fun forceFlush(): OperationResultCode =
        runWithTimeout(BatchTelemetryDefaults.FORCE_FLUSH_TIMEOUT_MS, closeable::forceFlush)

    override suspend fun shutdown(): OperationResultCode =
        shutdownState.shutdown(BatchTelemetryDefaults.SHUTDOWN_TIMEOUT_MS, closeable::shutdown)
}
