package io.opentelemetry.kotlin

import io.opentelemetry.kotlin.clock.FakeClock
import io.opentelemetry.kotlin.framework.SdkTracker
import io.opentelemetry.kotlin.logging.export.FakeLogRecordProcessor
import io.opentelemetry.kotlin.tracing.export.FakeSpanProcessor
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertSame

@OptIn(ExperimentalApi::class)
internal class CreateOpenTelemetryClockTest {

    private val sdks = SdkTracker()

    @AfterTest
    fun tearDown() = sdks.shutdownAll()

    @Test
    fun `default clock is used when none is configured`() {
        val api = createOpenTelemetry().also(sdks::track)
        assertIs<ClockImpl>(api.clock)
    }

    @Test
    fun `clock supplied via the DSL is used`() {
        val clock = FakeClock()
        val api = createOpenTelemetry { clock(clock) }.also(sdks::track)
        assertSame(clock, api.clock)
    }

    @Test
    fun `export DSL reads a clock configured after the providers`() {
        val clock = FakeClock(time = 42)
        var spanExportClock: Clock? = null
        var logExportClock: Clock? = null
        createOpenTelemetry {
            tracerProvider {
                export {
                    spanExportClock = this.clock
                    FakeSpanProcessor()
                }
            }
            loggerProvider {
                export {
                    logExportClock = this.clock
                    FakeLogRecordProcessor()
                }
            }
            clock(clock)
        }.also(sdks::track)

        assertEquals(42, spanExportClock?.now())
        assertEquals(42, logExportClock?.now())
    }
}
