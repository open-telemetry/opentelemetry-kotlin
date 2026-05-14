package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.ThreadSafe
import io.opentelemetry.kotlin.aliases.OtelJavaMeterProvider
import io.opentelemetry.kotlin.attributes.AttributesMutator
import java.util.concurrent.ConcurrentHashMap

@ThreadSafe
@ExperimentalApi
internal class MeterProviderAdapter(
    private val impl: OtelJavaMeterProvider
) : MeterProvider {

    private val map = ConcurrentHashMap<String, MeterAdapter>()

    override fun getMeter(
        name: String,
        version: String?,
        schemaUrl: String?,
        attributes: (AttributesMutator.() -> Unit)?,
    ): Meter {
        val key = name.plus(version).plus(schemaUrl)
        return map.getOrPut(key) {
            val builder = impl.meterBuilder(name)
            schemaUrl?.let(builder::setSchemaUrl)
            version?.let(builder::setInstrumentationVersion)
            MeterAdapter(builder.build())
        }
    }
}
