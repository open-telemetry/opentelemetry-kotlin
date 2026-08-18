package io.opentelemetry.kotlin.tracing

import io.opentelemetry.kotlin.Clock
import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.InstrumentationScopeInfo
import io.opentelemetry.kotlin.aliases.OtelJavaTracerProvider
import io.opentelemetry.kotlin.attributes.AttributesMutator
import io.opentelemetry.kotlin.init.CompatSpanLimitsConfig
import io.opentelemetry.kotlin.scope.scopeCacheKey
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.write

@ExperimentalApi
internal class TracerProviderAdapter(
    private val tracerProvider: OtelJavaTracerProvider,
    private val clock: Clock,
    private val spanLimitsConfig: CompatSpanLimitsConfig,
) : TracerProvider {

    private val map = ConcurrentHashMap<InstrumentationScopeInfo, TracerAdapter>()
    private val lock = ReentrantReadWriteLock()

    override fun getTracer(
        name: String,
        version: String?,
        schemaUrl: String?,
        attributes: (AttributesMutator.() -> Unit)?
    ): Tracer {
        val key = scopeCacheKey(name, version, schemaUrl)
        map[key]?.let { return it }
        val tracerBuilder = tracerProvider.tracerBuilder(name)
        schemaUrl?.let(tracerBuilder::setSchemaUrl)
        version?.let(tracerBuilder::setInstrumentationVersion)
        val candidate = TracerAdapter(tracerBuilder.build(), clock, spanLimitsConfig)
        return lock.write { map[key] ?: candidate.also { map[key] = it } }
    }
}
