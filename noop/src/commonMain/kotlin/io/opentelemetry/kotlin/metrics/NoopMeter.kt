package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.ExperimentalApi

/**
 * No-op implementation of [Meter].
 *
 * This implementation should induce as close to zero overhead as possible.
 */
@OptIn(ExperimentalApi::class)
internal object NoopMeter : Meter {
    override fun createLongGauge(name: String, unit: String?, description: String?): LongGauge =
        NoopLongGauge

    override fun createDoubleGauge(name: String, unit: String?, description: String?): DoubleGauge =
        NoopDoubleGauge

    override fun createDoubleUpDownCounter(
        name: String,
        unit: String?,
        description: String?,
    ): DoubleUpDownCounter = NoopDoubleUpDownCounter

    override fun createLongUpDownCounter(
        name: String,
        unit: String?,
        description: String?,
    ): LongUpDownCounter = NoopLongUpDownCounter

    override fun createObservableDoubleCounter(
        name: String,
        unit: String?,
        description: String?,
        vararg callbacks: (ObservableDoubleMeasurement) -> Unit,
    ): ObservableDoubleCounter = NoopObservableDoubleCounter

    override fun createObservableLongCounter(
        name: String,
        unit: String?,
        description: String?,
        vararg callbacks: (ObservableLongMeasurement) -> Unit,
    ): ObservableLongCounter = NoopObservableLongCounter

    override fun createObservableLongUpDownCounter(
        name: String,
        unit: String?,
        description: String?,
        vararg callbacks: (ObservableLongMeasurement) -> Unit,
    ): ObservableLongUpDownCounter = NoopObservableLongUpDownCounter

    override fun createObservableDoubleUpDownCounter(
        name: String,
        unit: String?,
        description: String?,
        vararg callbacks: (ObservableDoubleMeasurement) -> Unit,
    ): ObservableDoubleUpDownCounter = NoopObservableDoubleUpDownCounter

    override fun createObservableLongGauge(
        name: String,
        unit: String?,
        description: String?,
        vararg callbacks: (ObservableLongMeasurement) -> Unit,
    ): ObservableLongGauge = NoopObservableLongGauge

    override fun createObservableDoubleGauge(
        name: String,
        unit: String?,
        description: String?,
        vararg callbacks: (ObservableDoubleMeasurement) -> Unit,
    ): ObservableDoubleGauge = NoopObservableDoubleGauge

    override fun batchCallback(
        measurement: ObservableMeasurement,
        vararg additionalMeasurements: ObservableMeasurement,
        callback: () -> Unit,
    ): AutoCloseable = NoopBatchCallback
}

private object NoopBatchCallback : AutoCloseable {
    override fun close() = Unit
}
