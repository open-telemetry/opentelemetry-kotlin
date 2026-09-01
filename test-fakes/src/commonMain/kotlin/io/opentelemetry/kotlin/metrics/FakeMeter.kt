package io.opentelemetry.kotlin.metrics

class FakeMeter(
    val name: String
) : Meter {

    val doubleUpDownCounters: MutableList<FakeDoubleUpDownCounter> = mutableListOf()

    override fun createDoubleUpDownCounter(
        name: String,
        unit: String?,
        description: String?,
    ): DoubleUpDownCounter = FakeDoubleUpDownCounter(name, unit, description).also {
        doubleUpDownCounters.add(it)
    }
}
