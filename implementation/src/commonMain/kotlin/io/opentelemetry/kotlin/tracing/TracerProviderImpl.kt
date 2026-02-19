package io.opentelemetry.kotlin.tracing

import io.opentelemetry.kotlin.Clock
import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.attributes.MutableAttributeContainer
import io.opentelemetry.kotlin.export.DelegatingTelemetryCloseable
import io.opentelemetry.kotlin.export.TelemetryCloseable
import io.opentelemetry.kotlin.factory.SdkFactory
import io.opentelemetry.kotlin.init.config.TracingConfig
import io.opentelemetry.kotlin.provider.ApiProviderImpl
import io.opentelemetry.kotlin.tracing.export.createCompositeSpanProcessor

@OptIn(ExperimentalApi::class)
internal class TracerProviderImpl(
    private val clock: Clock,
    tracingConfig: TracingConfig,
    sdkFactory: SdkFactory,
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
            sdkFactory = sdkFactory,
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
