package io.opentelemetry.kotlin.metrics

class FakeMeter(
    val name: String
) : Meter {

    val longUpDownCounters: MutableList<FakeLongUpDownCounter> = mutableListOf()
    val doubleUpDownCounters: MutableList<FakeDoubleUpDownCounter> = mutableListOf()

    override fun createLongUpDownCounter(
        name: String,
        unit: String?,
        description: String?,
    ): LongUpDownCounter = FakeLongUpDownCounter(name, unit, description).also {
        longUpDownCounters.add(it)
    }

    override fun createDoubleUpDownCounter(
        name: String,
        unit: String?,
        description: String?,
    ): DoubleUpDownCounter = FakeDoubleUpDownCounter(name, unit, description).also {
        doubleUpDownCounters.add(it)
    }
}
