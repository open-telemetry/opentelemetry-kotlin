package io.opentelemetry.kotlin.init

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.clock.FakeClock
import io.opentelemetry.kotlin.context.ImplicitContextStorageMode
import io.opentelemetry.kotlin.logging.export.FakeLogRecordProcessor
import io.opentelemetry.kotlin.tracing.export.FakeSpanProcessor
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalApi::class)
internal class OpenTelemetryConfigImplTest {

    private val clock = FakeClock()

    @Test
    fun testDefaultConfig() {
        val cfg = OpenTelemetryConfigImpl(clock)
        assertTrue(cfg.tracingConfig.generateTracingConfig().processors.isEmpty())
        assertTrue(cfg.loggingConfig.generateLoggingConfig().processors.isEmpty())
        assertEquals(ImplicitContextStorageMode.GLOBAL, cfg.contextConfig.storageMode)
    }

    @Test
    fun testOverrideConfig() {
        val cfg = OpenTelemetryConfigImpl(clock)
        cfg.loggerProvider {
            export { FakeLogRecordProcessor() }
        }
        cfg.tracerProvider {
            export { FakeSpanProcessor() }
        }
        cfg.context {
            assertEquals(ImplicitContextStorageMode.GLOBAL, storageMode)
        }
        assertFalse(cfg.tracingConfig.generateTracingConfig().processors.isEmpty())
        assertFalse(cfg.loggingConfig.generateLoggingConfig().processors.isEmpty())
    }
}
