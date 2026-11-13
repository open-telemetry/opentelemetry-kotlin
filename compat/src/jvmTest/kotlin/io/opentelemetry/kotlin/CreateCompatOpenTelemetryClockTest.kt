package io.opentelemetry.kotlin

import io.opentelemetry.kotlin.clock.FakeClock
import io.opentelemetry.kotlin.framework.SdkTracker
import io.opentelemetry.kotlin.tracing.export.FakeSpanProcessor
import org.junit.Test
import kotlin.test.AfterTest
import kotlin.test.assertEquals
import kotlin.test.assertSame

@OptIn(ExperimentalApi::class)
internal class CreateCompatOpenTelemetryClockTest {

    private val sdks = SdkTracker()

    @AfterTest
    fun tearDown() = sdks.shutdownAll()

    @Test
    fun `clock configured after the tracer provider times spans and export DSL`() {
        val clock = FakeClock(time = 42)
        val processor = FakeSpanProcessor()
        var exportClock: Clock? = null
        val api = createCompatOpenTelemetry {
            tracerProvider {
                export {
                    exportClock = this.clock
                    processor
                }
            }
            clock(clock)
        }.also(sdks::track)

        api.tracerProvider.getTracer("test").startSpan("span").end()

        assertSame(clock, api.clock)
        assertEquals(42, exportClock?.now())
        assertEquals(42, processor.endCalls.single().startTimestamp)
    }
}
