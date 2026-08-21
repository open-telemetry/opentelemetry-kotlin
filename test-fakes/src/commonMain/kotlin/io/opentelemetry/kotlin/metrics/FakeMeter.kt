package io.opentelemetry.kotlin.metrics

class FakeMeter(
    val name: String
) : Meter {

    val longUpDownCounters: MutableList<FakeLongUpDownCounter> = mutableListOf()

    override fun createLongUpDownCounter(
        name: String,
        unit: String?,
        description: String?,
    ): LongUpDownCounter = FakeLongUpDownCounter(name, unit, description).also {
        longUpDownCounters.add(it)
    }
}
