package io.opentelemetry.kotlin.init

import io.opentelemetry.kotlin.Clock
import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.behavior.LogLimitsBehavior
import io.opentelemetry.kotlin.behavior.SpanLimitsBehavior
import io.opentelemetry.kotlin.clock.ClockAdapter
import io.opentelemetry.kotlin.clock.FakeClock
import io.opentelemetry.kotlin.error.NoopSdkErrorHandler
import io.opentelemetry.kotlin.factory.CompatIdGenerator
import io.opentelemetry.kotlin.fakes.otel.java.FakeOtelJavaClock
import io.opentelemetry.kotlin.logging.export.FakeLogRecordProcessor
import io.opentelemetry.kotlin.tracing.data.SpanData
import io.opentelemetry.kotlin.tracing.export.FakeSpanProcessor
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

@OptIn(ExperimentalApi::class)
internal class CompatClockTest {

    private val processor = FakeSpanProcessor()

    private fun startSpan(clock: Clock) = CompatTracerProviderConfig(clock, NoopSdkErrorHandler)
        .apply { export { processor } }
        .build(clock, CompatIdGenerator(), spanLimits = SpanLimitsBehavior())
        .getTracer("test")
        .startSpan("span")

    private val ended: SpanData
        get() = processor.endCalls.single()

    @Test
    fun `adapted java clock is handed to the sdk unwrapped`() {
        val javaClock = FakeOtelJavaClock()
        assertSame(javaClock, ClockAdapter(javaClock).toOtelJavaClock())
    }

    @Test
    fun `custom clock stands in for both sdk readings`() {
        val javaClock = FakeClock(time = 100).toOtelJavaClock()
        assertEquals(100, javaClock.now())
        assertEquals(100, javaClock.nanoTime())
    }

    @Test
    fun `default clock times span duration monotonically`() {
        val javaClock = FakeOtelJavaClock(start = 1_000_000)
        val span = startSpan(ClockAdapter(javaClock))
        javaClock.nanoseconds -= 1_000
        javaClock.advance(5)
        span.end()
        assertEquals(1_000_000, ended.startTimestamp)
        assertEquals(1_000_005, ended.endTimestamp)
    }

    @Test
    fun `custom clock times spans`() {
        val clock = FakeClock(time = 100)
        val span = startSpan(clock)
        clock.time = 150
        span.addEvent("event")
        span.end()

        assertEquals(100, ended.startTimestamp)
        assertEquals(150, ended.events.single().timestamp)
        assertEquals(150, ended.endTimestamp)
    }

    @Test
    fun `custom clock times logs`() {
        val clock = FakeClock(time = 100)
        val logProcessor = FakeLogRecordProcessor()
        CompatLoggerProviderConfig(clock, NoopSdkErrorHandler)
            .apply { export { logProcessor } }
            .build(clock, logLimits = LogLimitsBehavior())
            .getLogger("test")
            .emit(body = "log")
        assertEquals(100L, logProcessor.logs.single().observedTimestamp)
    }
}
