package io.opentelemetry.kotlin.clock

import io.opentelemetry.kotlin.Clock
import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaClock

@OptIn(ExperimentalApi::class)
internal class ClockAdapter(
    private val clock: OtelJavaClock = OtelJavaClock.getDefault()
) : Clock {
    override fun now(): Long = clock.now()
}
