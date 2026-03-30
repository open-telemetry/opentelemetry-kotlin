package io.opentelemetry.kotlin.init

import io.opentelemetry.kotlin.attributes.DEFAULT_ATTRIBUTE_LIMIT
import io.opentelemetry.kotlin.attributes.DEFAULT_ATTRIBUTE_VALUE_LENGTH_LIMIT
import io.opentelemetry.kotlin.clock.FakeClock
import io.opentelemetry.kotlin.context.ImplicitContextStorageMode
import io.opentelemetry.kotlin.logging.export.FakeLogRecordProcessor
import io.opentelemetry.kotlin.tracing.export.FakeSpanProcessor
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class OpenTelemetryConfigImplTest {

    private val clock = FakeClock()

    @Test
    fun testDefaultConfig() {
        val cfg = OpenTelemetryConfigImpl(clock)
        assertTrue(cfg.generateTracingConfig().processors.isEmpty())
        assertTrue(cfg.generateLoggingConfig().processors.isEmpty())
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
        assertFalse(cfg.generateTracingConfig().processors.isEmpty())
        assertFalse(cfg.generateLoggingConfig().processors.isEmpty())
    }

    @Test
    fun testGlobalAttrLimits() {
        val cfg = OpenTelemetryConfigImpl(clock).apply {
            attributeLimits {
                attributeCountLimit = 64
            }
        }
        assertEquals(64, cfg.generateTracingConfig().spanLimits.attributeCountLimit)
        assertEquals(64, cfg.generateLoggingConfig().logLimits.attributeCountLimit)
    }

    @Test
    fun testLocalAttrLimits() {
        val cfg = OpenTelemetryConfigImpl(clock).apply {
            attributeLimits {
                attributeCountLimit = 64
            }
            tracerProvider {
                spanLimits {
                    attributeCountLimit = 32
                }
            }
        }
        assertEquals(32, cfg.generateTracingConfig().spanLimits.attributeCountLimit)
        assertEquals(64, cfg.generateLoggingConfig().logLimits.attributeCountLimit)
    }

    @Test
    fun testLocalAttrLimits2() {
        val cfg = OpenTelemetryConfigImpl(clock).apply {
            attributeLimits {
                attributeCountLimit = 64
            }
            tracerProvider {
                spanLimits {
                    attributeValueLengthLimit = 256
                }
            }
        }
        with(cfg.generateTracingConfig().spanLimits) {
            assertEquals(64, attributeCountLimit)
            assertEquals(256, attributeValueLengthLimit)
        }
        assertEquals(64, cfg.generateLoggingConfig().logLimits.attributeCountLimit)
    }

    @Test
    fun testDefaultAttrLimits() {
        val cfg = OpenTelemetryConfigImpl(clock)
        with(cfg.generateTracingConfig().spanLimits) {
            assertEquals(DEFAULT_ATTRIBUTE_LIMIT, attributeCountLimit)
            assertEquals(DEFAULT_ATTRIBUTE_VALUE_LENGTH_LIMIT, attributeValueLengthLimit)
        }
        with(cfg.generateLoggingConfig().logLimits) {
            assertEquals(DEFAULT_ATTRIBUTE_LIMIT, attributeCountLimit)
            assertEquals(DEFAULT_ATTRIBUTE_VALUE_LENGTH_LIMIT, attributeValueLengthLimit)
        }
    }
}
