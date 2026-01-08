package io.opentelemetry.kotlin.init

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.context.ImplicitContextStorageMode
import io.opentelemetry.kotlin.logging.export.FakeLogRecordProcessor
import io.opentelemetry.kotlin.tracing.export.FakeSpanProcessor
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@OptIn(ExperimentalApi::class)
internal class OpenTelemetryConfigImplTest {

    @Test
    fun testDefaultConfig() {
        val cfg = OpenTelemetryConfigImpl()
        assertTrue(cfg.tracingConfig.generateTracingConfig().processors.isEmpty())
        assertTrue(cfg.loggingConfig.generateLoggingConfig().processors.isEmpty())
        assertEquals(ImplicitContextStorageMode.GLOBAL, cfg.contextConfig.storageMode)
        assertNotNull(cfg.clock)
    }

    @Test
    fun testOverrideConfig() {
        val cfg = OpenTelemetryConfigImpl()
        cfg.loggerProvider { addLogRecordProcessor(FakeLogRecordProcessor()) }
        cfg.tracerProvider { addSpanProcessor(FakeSpanProcessor()) }
        cfg.context {
            assertEquals(ImplicitContextStorageMode.GLOBAL, storageMode)
        }
        assertFalse(cfg.tracingConfig.generateTracingConfig().processors.isEmpty())
        assertFalse(cfg.loggingConfig.generateLoggingConfig().processors.isEmpty())
        assertNotNull(cfg.clock)
    }
}
