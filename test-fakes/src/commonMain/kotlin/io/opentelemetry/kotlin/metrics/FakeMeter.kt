package io.opentelemetry.kotlin.metrics

class FakeMeter(
    val name: String
) : Meter {

    val doubleUpDownCounters: MutableList<FakeDoubleUpDownCounter> = mutableListOf()
    val longUpDownCounters: MutableList<FakeLongUpDownCounter> = mutableListOf()
    val observableDoubleCounters: MutableList<FakeObservableDoubleCounter> = mutableListOf()
    val observableLongCounters: MutableList<FakeObservableLongCounter> = mutableListOf()
    val observableLongUpDownCounters: MutableList<FakeObservableLongUpDownCounter> = mutableListOf()
    val observableDoubleUpDownCounters: MutableList<FakeObservableDoubleUpDownCounter> = mutableListOf()
    val observableLongGauges: MutableList<FakeObservableLongGauge> = mutableListOf()
    val observableDoubleGauges: MutableList<FakeObservableDoubleGauge> = mutableListOf()
    val longGauges: MutableList<FakeLongGauge> = mutableListOf()

    override fun createLongGauge(name: String, unit: String?, description: String?): LongGauge =
        FakeLongGauge(name, unit, description).also { longGauges.add(it) }

    val doubleGauges: MutableList<FakeDoubleGauge> = mutableListOf()

    override fun createDoubleGauge(name: String, unit: String?, description: String?): DoubleGauge =
        FakeDoubleGauge(name, unit, description).also { doubleGauges.add(it) }

    override fun createDoubleUpDownCounter(
        name: String,
        unit: String?,
        description: String?,
    ): DoubleUpDownCounter = FakeDoubleUpDownCounter(name, unit, description).also {
        doubleUpDownCounters.add(it)
    }

    override fun createLongUpDownCounter(
        name: String,
        unit: String?,
        description: String?,
    ): LongUpDownCounter = FakeLongUpDownCounter(name, unit, description).also {
        longUpDownCounters.add(it)
    }

    override fun createObservableDoubleCounter(
        name: String,
        unit: String?,
        description: String?,
        vararg callbacks: (ObservableDoubleMeasurement) -> Unit,
    ): ObservableDoubleCounter = FakeObservableDoubleCounter(name, unit, description, callbacks.toList()).also {
        observableDoubleCounters.add(it)
    }

    override fun createObservableLongCounter(
        name: String,
        unit: String?,
        description: String?,
        vararg callbacks: (ObservableLongMeasurement) -> Unit,
    ): ObservableLongCounter = FakeObservableLongCounter(name, unit, description, callbacks.toList()).also {
        observableLongCounters.add(it)
    }

    override fun createObservableLongUpDownCounter(
        name: String,
        unit: String?,
        description: String?,
        vararg callbacks: (ObservableLongMeasurement) -> Unit,
    ): ObservableLongUpDownCounter =
        FakeObservableLongUpDownCounter(name, unit, description, callbacks.toList()).also {
            observableLongUpDownCounters.add(it)
        }

    override fun createObservableDoubleUpDownCounter(
        name: String,
        unit: String?,
        description: String?,
        vararg callbacks: (ObservableDoubleMeasurement) -> Unit,
    ): ObservableDoubleUpDownCounter =
        FakeObservableDoubleUpDownCounter(name, unit, description, callbacks.toList()).also {
            observableDoubleUpDownCounters.add(it)
        }

    override fun createObservableLongGauge(
        name: String,
        unit: String?,
        description: String?,
        vararg callbacks: (ObservableLongMeasurement) -> Unit,
    ): ObservableLongGauge = FakeObservableLongGauge(name, unit, description, callbacks.toList()).also {
        observableLongGauges.add(it)
    }

    override fun createObservableDoubleGauge(
        name: String,
        unit: String?,
        description: String?,
        vararg callbacks: (ObservableDoubleMeasurement) -> Unit,
    ): ObservableDoubleGauge = FakeObservableDoubleGauge(name, unit, description, callbacks.toList()).also {
        observableDoubleGauges.add(it)
    }

    val batchCallbacks: MutableList<FakeBatchCallback> = mutableListOf()

    override fun batchCallback(
        measurement: ObservableMeasurement,
        vararg additionalMeasurements: ObservableMeasurement,
        callback: () -> Unit,
    ): AutoCloseable = FakeBatchCallback(
        measurements = listOf(measurement) + additionalMeasurements,
        callback = callback,
        onClose = { batchCallbacks.remove(it) },
    ).also(batchCallbacks::add)
}
