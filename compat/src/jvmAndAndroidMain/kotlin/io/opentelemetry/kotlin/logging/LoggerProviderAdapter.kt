package io.opentelemetry.kotlin.logging

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.InstrumentationScopeInfo
import io.opentelemetry.kotlin.aliases.OtelJavaLoggerProvider
import io.opentelemetry.kotlin.attributes.AttributesMutator
import io.opentelemetry.kotlin.scope.scopeCacheKey
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.write

@ExperimentalApi
internal class LoggerProviderAdapter(private val impl: OtelJavaLoggerProvider) : LoggerProvider {

    private val map = ConcurrentHashMap<InstrumentationScopeInfo, LoggerAdapter>()
    private val lock = ReentrantReadWriteLock()

    override fun getLogger(
        name: String,
        version: String?,
        schemaUrl: String?,
        attributes: (AttributesMutator.() -> Unit)?
    ): Logger {
        val key = scopeCacheKey(name, version, schemaUrl)
        map[key]?.let { return it }
        val builder = impl.loggerBuilder(name)
        if (schemaUrl != null) {
            builder.setSchemaUrl(schemaUrl)
        }
        if (version != null) {
            builder.setInstrumentationVersion(version)
        }
        val candidate = LoggerAdapter(builder.build())
        return lock.write { map[key] ?: candidate.also { map[key] = it } }
    }
}
