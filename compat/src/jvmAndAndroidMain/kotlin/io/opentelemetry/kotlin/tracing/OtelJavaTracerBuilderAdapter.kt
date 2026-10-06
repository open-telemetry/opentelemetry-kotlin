package io.opentelemetry.kotlin.tracing

import io.opentelemetry.kotlin.aliases.OtelJavaTracer
import io.opentelemetry.kotlin.aliases.OtelJavaTracerBuilder
import io.opentelemetry.kotlin.factory.ContextFactory

internal class OtelJavaTracerBuilderAdapter(
    private val tracerProvider: TracerProvider,
    private val instrumentationScopeName: String,
    private val contextFactory: ContextFactory,
) : OtelJavaTracerBuilder {
    private var instrumentationScopeVersion: String? = null
    private var schemaUrl: String? = null

    override fun setInstrumentationVersion(instrumentationScopeVersion: String): OtelJavaTracerBuilder {
        this.instrumentationScopeVersion = instrumentationScopeVersion
        return this
    }

    override fun setSchemaUrl(schemaUrl: String): OtelJavaTracerBuilder {
        this.schemaUrl = schemaUrl
        return this
    }

    override fun build(): OtelJavaTracer {
        val tracer = tracerProvider.getTracer(
            name = instrumentationScopeName,
            version = instrumentationScopeVersion,
            schemaUrl = schemaUrl
        )
        return OtelJavaTracerAdapter(tracer, contextFactory)
    }
}
