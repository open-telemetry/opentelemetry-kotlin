package io.opentelemetry.kotlin.clock

import io.opentelemetry.kotlin.Clock
import io.opentelemetry.kotlin.ExperimentalApi

@OptIn(ExperimentalApi::class)
class FakeClock(
    var time: Long = 0
) : Clock {
    override fun now(): Long = time
}
