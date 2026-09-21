package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.InstrumentationScopeInfo
import io.opentelemetry.kotlin.ThreadSafe
import io.opentelemetry.kotlin.aliases.OtelJavaMeterProvider
import io.opentelemetry.kotlin.aliases.OtelJavaSdkMeterProvider
import io.opentelemetry.kotlin.attributes.AttributesMutator
import io.opentelemetry.kotlin.awaitOperationResultCode
import io.opentelemetry.kotlin.export.OperationResultCode
import io.opentelemetry.kotlin.export.TelemetryCloseable
import io.opentelemetry.kotlin.scope.scopeCacheKey
import java.util.concurrent.ConcurrentHashMap

@ThreadSafe
@ExperimentalApi
internal class MeterProviderAdapter(
    private val impl: OtelJavaMeterProvider
) : MeterProvider, TelemetryCloseable {

    private val map = ConcurrentHashMap<InstrumentationScopeInfo, MeterAdapter>()

    override fun getMeter(
        name: String,
        version: String?,
        schemaUrl: String?,
        attributes: (AttributesMutator.() -> Unit)?,
    ): Meter {
        return map.getOrPut(scopeCacheKey(name, version, schemaUrl, attributes)) {
            val builder = impl.meterBuilder(name)
            schemaUrl?.let(builder::setSchemaUrl)
            version?.let(builder::setInstrumentationVersion)
            MeterAdapter(builder.build())
        }
    }

    override suspend fun forceFlush(): OperationResultCode = when (impl) {
        is OtelJavaSdkMeterProvider -> awaitOperationResultCode { impl.forceFlush() }
        else -> OperationResultCode.Success
    }

    override suspend fun shutdown(): OperationResultCode = when (impl) {
        is OtelJavaSdkMeterProvider -> awaitOperationResultCode { impl.shutdown() }
        else -> OperationResultCode.Success
    }
}
