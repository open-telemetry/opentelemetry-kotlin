package io.opentelemetry.kotlin.clock

import io.opentelemetry.kotlin.Clock

class FakeClock(
    var time: Long = 0
) : Clock {
    override fun now(): Long = time
}
