package io.opentelemetry.kotlin.tracing

import io.opentelemetry.kotlin.Clock
import io.opentelemetry.kotlin.attributes.AttributesMutator
import io.opentelemetry.kotlin.behavior.AttributeLimitsBehavior
import io.opentelemetry.kotlin.behavior.SpanLimitsBehavior
import io.opentelemetry.kotlin.error.SdkError
import io.opentelemetry.kotlin.error.SdkErrorSeverity
import io.opentelemetry.kotlin.error.guardOrDefault
import io.opentelemetry.kotlin.error.reportError
import io.opentelemetry.kotlin.error.sdkGuardOrDefault
import io.opentelemetry.kotlin.error.sdkGuardOrDefaultSuspend
import io.opentelemetry.kotlin.error.userCode
import io.opentelemetry.kotlin.export.BatchTelemetryDefaults
import io.opentelemetry.kotlin.export.CompositeTelemetryCloseable
import io.opentelemetry.kotlin.export.MutableShutdownState
import io.opentelemetry.kotlin.export.OperationResultCode
import io.opentelemetry.kotlin.export.TelemetryCloseable
import io.opentelemetry.kotlin.export.runWithTimeout
import io.opentelemetry.kotlin.factory.IdGenerator
import io.opentelemetry.kotlin.init.config.DefaultSampler
import io.opentelemetry.kotlin.init.config.SpanLimitConfig
import io.opentelemetry.kotlin.init.config.TracingConfig
import io.opentelemetry.kotlin.provider.ApiProviderImpl
import io.opentelemetry.kotlin.tracing.sampling.SamplerCloseable
import io.opentelemetry.kotlin.tracing.sampling.guarded

internal class TracerProviderImpl(
    private val clock: Clock,
    tracingConfig: TracingConfig,
    private val idGenerator: IdGenerator,
    private val attributeLimits: AttributeLimitsBehavior,
    spanLimits: SpanLimitsBehavior,
) : TracerProvider, TelemetryCloseable {

    private val sdkErrorHandler = tracingConfig.sdkErrorHandler
    private val shutdownState: MutableShutdownState = MutableShutdownState()
    private val sampler = sdkErrorHandler.guardOrDefault(DefaultSampler, "Failed to create sampler, using default") {
        tracingConfig.samplerFactory()
    }.guarded(sdkErrorHandler)

    // Processors shut down first so that any spans still in flight finish before the sampler
    // releases its resources.
    private val closeable: TelemetryCloseable = CompositeTelemetryCloseable(
        listOfNotNull(tracingConfig.processor, SamplerCloseable(sampler)),
        sdkErrorHandler,
    )
    private val noopTracer = PropagatingNoopTracer(sdkErrorHandler)
    private val spanLimitConfig = SpanLimitConfig(spanLimits)

    private val apiProvider = ApiProviderImpl<Tracer> { key ->
        if (tracingConfig.processor == null) {
            return@ApiProviderImpl noopTracer
        }
        val tracerConfig = userCode { tracingConfig.tracerConfigurator.tracerConfig(key) }
        if (!tracerConfig.enabled) {
            noopTracer
        } else {
            TracerImpl(
                clock = clock,
                processor = tracingConfig.processor,
                scope = key,
                resource = tracingConfig.resource,
                spanLimitConfig = spanLimitConfig,
                idGenerator = idGenerator,
                shutdownState = shutdownState,
                sampler = sampler,
                sdkErrorHandler = tracingConfig.sdkErrorHandler,
            )
        }
    }

    override fun getTracer(
        name: String,
        version: String?,
        schemaUrl: String?,
        attributes: (AttributesMutator.() -> Unit)?
    ): Tracer =
        sdkErrorHandler.sdkGuardOrDefault(noopTracer, "TracerProvider.getTracer failed") {
            shutdownState.ifActiveOrElse(noopTracer) {
                if (name.isEmpty()) {
                    sdkErrorHandler.reportError(
                        SdkError.ApiMisuse(
                            api = "TracerProvider.getTracer",
                            message = "Tracer requested without instrumentation scope name",
                            severity = SdkErrorSeverity.WARNING,
                        )
                    )
                }
                val key = apiProvider.createInstrumentationScopeInfo(
                    name = name,
                    version = version,
                    schemaUrl = schemaUrl,
                    attributes = attributes,
                    attributeLimits = attributeLimits,
                )
                apiProvider.getOrCreate(key)
            }
        }

    override suspend fun forceFlush(): OperationResultCode =
        sdkErrorHandler.sdkGuardOrDefaultSuspend(OperationResultCode.Failure, "TracerProvider.forceFlush failed") {
            runWithTimeout(BatchTelemetryDefaults.FORCE_FLUSH_TIMEOUT_MS, closeable::forceFlush)
        }

    override suspend fun shutdown(): OperationResultCode =
        sdkErrorHandler.sdkGuardOrDefaultSuspend(OperationResultCode.Failure, "TracerProvider.shutdown failed") {
            shutdownState.shutdown(BatchTelemetryDefaults.SHUTDOWN_TIMEOUT_MS, closeable::shutdown)
        }
}
