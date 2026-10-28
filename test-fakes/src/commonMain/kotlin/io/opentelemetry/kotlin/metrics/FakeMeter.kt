package io.opentelemetry.kotlin.metrics

class FakeMeter(
    val name: String
) : Meter {

    val doubleUpDownCounters: MutableList<FakeDoubleUpDownCounter> = mutableListOf()
    val longUpDownCounters: MutableList<FakeLongUpDownCounter> = mutableListOf()
    val observableDoubleCounters: MutableList<FakeObservableDoubleCounter> = mutableListOf()
    val observableLongCounters: MutableList<FakeObservableLongCounter> = mutableListOf()

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
}
