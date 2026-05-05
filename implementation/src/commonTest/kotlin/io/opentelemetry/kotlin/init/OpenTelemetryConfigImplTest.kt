package io.opentelemetry.kotlin.init

import io.opentelemetry.kotlin.NoopOpenTelemetry
import io.opentelemetry.kotlin.attributes.DEFAULT_ATTRIBUTE_LIMIT
import io.opentelemetry.kotlin.attributes.DEFAULT_ATTRIBUTE_VALUE_LENGTH_LIMIT
import io.opentelemetry.kotlin.clock.FakeClock
import io.opentelemetry.kotlin.context.DefaultImplicitContextStorage
import io.opentelemetry.kotlin.context.FakeContext
import io.opentelemetry.kotlin.context.FakeImplicitContextStorage
import io.opentelemetry.kotlin.context.ImplicitContextStorageMode
import io.opentelemetry.kotlin.logging.export.FakeLogRecordProcessor
import io.opentelemetry.kotlin.propagation.CompositeTextMapPropagator
import io.opentelemetry.kotlin.propagation.W3CBaggagePropagator
import io.opentelemetry.kotlin.tracing.export.FakeSpanProcessor
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

internal class OpenTelemetryConfigImplTest {

    private val clock = FakeClock()

    @Test
    fun testDefaultConfig() {
        val cfg = OpenTelemetryConfigImpl(clock)
        assertNull(cfg.generateTracingConfig().processor)
        assertNull(cfg.generateLoggingConfig().processor)
        assertEquals(ImplicitContextStorageMode.GLOBAL, cfg.contextConfig.storageMode)
        assertSame(NoopOpenTelemetry.propagator, cfg.propagatorCfg.buildPropagator())
    }

    @Test
    fun testPropagatorOverride() {
        val cfg = OpenTelemetryConfigImpl(clock).apply {
            propagator { w3cBaggage() }
        }
        assertSame(W3CBaggagePropagator, cfg.propagatorCfg.buildPropagator())
    }

    @Test
    fun testCompositePropagatorOverride() {
        val cfg = OpenTelemetryConfigImpl(clock).apply {
            propagator { composite(w3cBaggage()) }
        }
        val composite = cfg.propagatorCfg.buildPropagator()
        assertIs<CompositeTextMapPropagator>(composite)
        assertEquals(listOf("baggage"), composite.fields().toList())
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
        assertNotNull(cfg.generateTracingConfig().processor)
        assertNotNull(cfg.generateLoggingConfig().processor)
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
    fun testDefaultStorage() {
        val cfg = OpenTelemetryConfigImpl(clock)
        val rootContext = FakeContext()
        val storage = cfg.contextConfig.generateStorage { rootContext }
        assertTrue(storage is DefaultImplicitContextStorage)
    }

    @Test
    fun testCustomStorage() {
        val custom = FakeImplicitContextStorage()
        val cfg = OpenTelemetryConfigImpl(clock).apply {
            context {
                storage { custom }
            }
        }
        assertSame(custom, cfg.contextConfig.generateStorage(::FakeContext))
    }

    @Test
    fun testCustomStorageOverridesStorageMode() {
        val custom = FakeImplicitContextStorage()
        val cfg = OpenTelemetryConfigImpl(clock).apply {
            context {
                storageMode = ImplicitContextStorageMode.GLOBAL
                storage { custom }
            }
        }
        assertSame(custom, cfg.contextConfig.generateStorage(::FakeContext))
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
