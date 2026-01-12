package io.opentelemetry.kotlin.tracing

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaTracer
import io.opentelemetry.kotlin.aliases.OtelJavaTracerBuilder
import io.opentelemetry.kotlin.aliases.OtelJavaTracerProvider

@OptIn(ExperimentalApi::class)
internal class OtelJavaTracerProviderAdapter(
    private val tracerProvider: TracerProvider
) : OtelJavaTracerProvider {

    override fun get(instrumentationScopeName: String): OtelJavaTracer {
        val tracer = tracerProvider.getTracer(instrumentationScopeName)
        return OtelJavaTracerAdapter(tracer)
    }

    override fun get(instrumentationScopeName: String, instrumentationScopeVersion: String): OtelJavaTracer {
        val tracer = tracerProvider.getTracer(instrumentationScopeName, instrumentationScopeVersion)
        return OtelJavaTracerAdapter(tracer)
    }

    override fun tracerBuilder(instrumentationScopeName: String): OtelJavaTracerBuilder {
        return OtelJavaTracerBuilderAdapter(tracerProvider, instrumentationScopeName)
    }
}
