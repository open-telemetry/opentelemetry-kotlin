package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.InstrumentationScopeInfo
import io.opentelemetry.kotlin.NoopOpenTelemetry
import io.opentelemetry.kotlin.ThreadSafe
import io.opentelemetry.kotlin.aliases.OtelJavaMeterProvider
import io.opentelemetry.kotlin.aliases.OtelJavaSdkMeterProvider
import io.opentelemetry.kotlin.attributes.AttributesMutator
import io.opentelemetry.kotlin.awaitOperationResultCode
import io.opentelemetry.kotlin.error.SdkErrorHandler
import io.opentelemetry.kotlin.error.guardOrDefaultSuspend
import io.opentelemetry.kotlin.error.sdkGuardOrDefault
import io.opentelemetry.kotlin.error.userCode
import io.opentelemetry.kotlin.export.OperationResultCode
import io.opentelemetry.kotlin.export.TelemetryCloseable
import io.opentelemetry.kotlin.scope.scopeCacheKey
import java.util.concurrent.ConcurrentHashMap

@ThreadSafe
@ExperimentalApi
internal class MeterProviderAdapter(
    private val impl: OtelJavaMeterProvider,
    private val sdkErrorHandler: SdkErrorHandler,
) : MeterProvider, TelemetryCloseable {

    private val map = ConcurrentHashMap<InstrumentationScopeInfo, MeterAdapter>()
    private val noopMeter = NoopOpenTelemetry.meterProvider.getMeter("")

    override fun getMeter(
        name: String,
        version: String?,
        schemaUrl: String?,
        attributes: (AttributesMutator.() -> Unit)?,
    ): Meter = sdkErrorHandler.sdkGuardOrDefault(noopMeter, "MeterProvider.getMeter failed") {
        val key = userCode { scopeCacheKey(name, version, schemaUrl, attributes) }
        map.getOrPut(key) {
            val builder = impl.meterBuilder(name)
            schemaUrl?.let(builder::setSchemaUrl)
            version?.let(builder::setInstrumentationVersion)
            MeterAdapter(builder.build(), sdkErrorHandler)
        }
    }

    override suspend fun forceFlush(): OperationResultCode = when (impl) {
        is OtelJavaSdkMeterProvider -> sdkErrorHandler.guardOrDefaultSuspend(
            OperationResultCode.Failure,
            "MeterProvider.forceFlush failed",
        ) {
            awaitOperationResultCode { impl.forceFlush() }
        }
        else -> OperationResultCode.Success
    }

    override suspend fun shutdown(): OperationResultCode = when (impl) {
        is OtelJavaSdkMeterProvider -> sdkErrorHandler.guardOrDefaultSuspend(
            OperationResultCode.Failure,
            "MeterProvider.shutdown failed",
        ) {
            awaitOperationResultCode { impl.shutdown() }
        }
        else -> OperationResultCode.Success
    }
}
