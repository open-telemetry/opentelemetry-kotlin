package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.NoopOpenTelemetry
import io.opentelemetry.kotlin.aliases.OtelJavaMeter
import io.opentelemetry.kotlin.error.SdkErrorHandler
import io.opentelemetry.kotlin.error.sdkGuardOrDefault

@ExperimentalApi
internal class MeterAdapter(
    private val impl: OtelJavaMeter,
    private val sdkErrorHandler: SdkErrorHandler,
) : Meter {
    private val noopMeter = NoopOpenTelemetry.meterProvider.getMeter("")

    override fun createDoubleUpDownCounter(
        name: String,
        unit: String?,
        description: String?,
    ): DoubleUpDownCounter =
        sdkErrorHandler.sdkGuardOrDefault(null, "Meter.createDoubleUpDownCounter failed") {
            val builder = impl.upDownCounterBuilder(name).ofDoubles()
            unit?.let(builder::setUnit)
            description?.let(builder::setDescription)
            DoubleUpDownCounterAdapter(builder.build(), name, unit, description, sdkErrorHandler)
        } ?: noopMeter.createDoubleUpDownCounter(name, unit, description)

    override fun createLongUpDownCounter(
        name: String,
        unit: String?,
        description: String?,
    ): LongUpDownCounter =
        sdkErrorHandler.sdkGuardOrDefault(null, "Meter.createLongUpDownCounter failed") {
            val builder = impl.upDownCounterBuilder(name)
            unit?.let(builder::setUnit)
            description?.let(builder::setDescription)
            LongUpDownCounterAdapter(builder.build(), name, unit, description, sdkErrorHandler)
        } ?: noopMeter.createLongUpDownCounter(name, unit, description)

    override fun createObservableDoubleCounter(
        name: String,
        unit: String?,
        description: String?,
        vararg callbacks: (ObservableDoubleMeasurement) -> Unit,
    ): ObservableDoubleCounter =
        sdkErrorHandler.sdkGuardOrDefault(null, "Meter.createObservableDoubleCounter failed") {
            ObservableDoubleCounterAdapter(impl, name, unit, description, callbacks, sdkErrorHandler)
        } ?: noopMeter.createObservableDoubleCounter(name, unit, description)

    override fun createObservableLongCounter(
        name: String,
        unit: String?,
        description: String?,
        vararg callbacks: (ObservableLongMeasurement) -> Unit,
    ): ObservableLongCounter =
        sdkErrorHandler.sdkGuardOrDefault(null, "Meter.createObservableLongCounter failed") {
            ObservableLongCounterAdapter(impl, name, unit, description, callbacks, sdkErrorHandler)
        } ?: noopMeter.createObservableLongCounter(name, unit, description)

    override fun createObservableLongUpDownCounter(
        name: String,
        unit: String?,
        description: String?,
        vararg callbacks: (ObservableLongMeasurement) -> Unit,
    ): ObservableLongUpDownCounter =
        sdkErrorHandler.sdkGuardOrDefault(null, "Meter.createObservableLongUpDownCounter failed") {
            ObservableLongUpDownCounterAdapter(impl, name, unit, description, callbacks, sdkErrorHandler)
        } ?: noopMeter.createObservableLongUpDownCounter(name, unit, description)
}
