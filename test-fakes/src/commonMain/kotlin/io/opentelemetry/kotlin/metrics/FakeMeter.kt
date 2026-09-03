package io.opentelemetry.kotlin.metrics

class FakeMeter(
    val name: String
) : Meter {

    val doubleUpDownCounters: MutableList<FakeDoubleUpDownCounter> = mutableListOf()
    val longUpDownCounters: MutableList<FakeLongUpDownCounter> = mutableListOf()

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
}
