package io.opentelemetry.kotlin.metrics

class FakeMeter(
    val name: String
) : Meter {
    override fun createLongCounter(
        name: String,
        description: String?,
        unit: String?
    ): LongCounter {
        return FakeLongCounter()
    }
}


