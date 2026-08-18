package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.InstrumentationScopeInfo
import io.opentelemetry.kotlin.ThreadSafe
import io.opentelemetry.kotlin.aliases.OtelJavaMeterProvider
import io.opentelemetry.kotlin.attributes.AttributesMutator
import io.opentelemetry.kotlin.scope.scopeCacheKey
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.write

@ThreadSafe
@ExperimentalApi
internal class MeterProviderAdapter(
    private val impl: OtelJavaMeterProvider
) : MeterProvider {

    private val map = ConcurrentHashMap<InstrumentationScopeInfo, MeterAdapter>()
    private val lock = ReentrantReadWriteLock()

    override fun getMeter(
        name: String,
        version: String?,
        schemaUrl: String?,
        attributes: (AttributesMutator.() -> Unit)?,
    ): Meter {
        val key = scopeCacheKey(name, version, schemaUrl)
        map[key]?.let { return it }
        val builder = impl.meterBuilder(name)
        schemaUrl?.let(builder::setSchemaUrl)
        version?.let(builder::setInstrumentationVersion)
        val candidate = MeterAdapter(builder.build())
        return lock.write { map[key] ?: candidate.also { map[key] = it } }
    }
}
