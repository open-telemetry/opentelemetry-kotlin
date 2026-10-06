package io.opentelemetry.kotlin.tracing

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.InstrumentationScopeInfo
import io.opentelemetry.kotlin.NoopOpenTelemetry
import io.opentelemetry.kotlin.aliases.OtelJavaSdkTracerProvider
import io.opentelemetry.kotlin.aliases.OtelJavaTracerProvider
import io.opentelemetry.kotlin.attributes.AttributesMutator
import io.opentelemetry.kotlin.awaitOperationResultCode
import io.opentelemetry.kotlin.error.SdkErrorHandler
import io.opentelemetry.kotlin.error.sdkGuardOrDefault
import io.opentelemetry.kotlin.error.userCode
import io.opentelemetry.kotlin.export.OperationResultCode
import io.opentelemetry.kotlin.export.TelemetryCloseable
import io.opentelemetry.kotlin.factory.ContextFactory
import io.opentelemetry.kotlin.init.CompatSpanLimitsConfig
import io.opentelemetry.kotlin.scope.scopeCacheKey
import java.util.concurrent.ConcurrentHashMap

@ExperimentalApi
internal class TracerProviderAdapter(
    private val tracerProvider: OtelJavaTracerProvider,
    private val spanLimitsConfig: CompatSpanLimitsConfig,
    private val contextFactory: ContextFactory,
    private val sdkErrorHandler: SdkErrorHandler,
) : TracerProvider, TelemetryCloseable {

    private val map = ConcurrentHashMap<InstrumentationScopeInfo, TracerAdapter>()
    private val noopTracer = NoopOpenTelemetry.tracerProvider.getTracer("")

    override fun getTracer(
        name: String,
        version: String?,
        schemaUrl: String?,
        attributes: (AttributesMutator.() -> Unit)?
    ): Tracer = sdkErrorHandler.sdkGuardOrDefault(noopTracer, "TracerProvider.getTracer failed") {
        val key = userCode { scopeCacheKey(name, version, schemaUrl, attributes) }
        map.getOrPut(key) {
            val tracerBuilder = tracerProvider.tracerBuilder(name)

            schemaUrl?.let(tracerBuilder::setSchemaUrl)
            version?.let(tracerBuilder::setInstrumentationVersion)
            val tracer = tracerBuilder.build()
            TracerAdapter(tracer, spanLimitsConfig, contextFactory, sdkErrorHandler)
        }
    }

    override suspend fun forceFlush(): OperationResultCode = when (tracerProvider) {
        is OtelJavaSdkTracerProvider -> awaitOperationResultCode { tracerProvider.forceFlush() }
        else -> OperationResultCode.Success
    }

    override suspend fun shutdown(): OperationResultCode = when (tracerProvider) {
        is OtelJavaSdkTracerProvider -> awaitOperationResultCode { tracerProvider.shutdown() }
        else -> OperationResultCode.Success
    }
}
