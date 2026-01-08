package io.opentelemetry.kotlin.fakes.otel.java

import io.opentelemetry.kotlin.aliases.OtelJavaClock

internal class FakeOtelJavaClock : OtelJavaClock {
    var nanoseconds = 0L
    override fun now(): Long = nanoseconds
    override fun nanoTime(): Long = nanoseconds
}
