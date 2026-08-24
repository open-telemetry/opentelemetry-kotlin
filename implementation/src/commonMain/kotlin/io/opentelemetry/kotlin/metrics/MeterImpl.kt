package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.InstrumentationScopeInfo
import io.opentelemetry.kotlin.NoopOpenTelemetry
import io.opentelemetry.kotlin.error.SdkErrorHandler
import io.opentelemetry.kotlin.resource.Resource

internal class MeterImpl(
    val instrumentationScopeInfo: InstrumentationScopeInfo,
    val resource: Resource,
    private val sdkErrorHandler: SdkErrorHandler,
) : Meter {
    override fun createDoubleUpDownCounter(
        name: String,
        unit: String?,
        description: String?,
    ): DoubleUpDownCounter {
        if (!sdkErrorHandler.isValidInstrumentName(name)) {
            return NoopOpenTelemetry.meterProvider.getMeter("")
                .createDoubleUpDownCounter(name, unit, description)
        }
        return DoubleUpDownCounterImpl(name, unit, description)
    }
}
