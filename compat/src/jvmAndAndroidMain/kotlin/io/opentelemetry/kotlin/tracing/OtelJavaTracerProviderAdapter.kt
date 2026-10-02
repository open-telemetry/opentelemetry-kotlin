package io.opentelemetry.kotlin.tracing

import io.opentelemetry.kotlin.aliases.OtelJavaTracer
import io.opentelemetry.kotlin.aliases.OtelJavaTracerBuilder
import io.opentelemetry.kotlin.aliases.OtelJavaTracerProvider
import io.opentelemetry.kotlin.factory.ContextFactory

internal class OtelJavaTracerProviderAdapter(
    private val tracerProvider: TracerProvider,
    private val contextFactory: ContextFactory,
) : OtelJavaTracerProvider {

    override fun get(instrumentationScopeName: String): OtelJavaTracer {
        val tracer = tracerProvider.getTracer(instrumentationScopeName)
        return OtelJavaTracerAdapter(tracer, contextFactory)
    }

    override fun get(instrumentationScopeName: String, instrumentationScopeVersion: String): OtelJavaTracer {
        val tracer = tracerProvider.getTracer(instrumentationScopeName, instrumentationScopeVersion)
        return OtelJavaTracerAdapter(tracer, contextFactory)
    }

    override fun tracerBuilder(instrumentationScopeName: String): OtelJavaTracerBuilder {
        return OtelJavaTracerBuilderAdapter(tracerProvider, instrumentationScopeName, contextFactory)
    }
}
