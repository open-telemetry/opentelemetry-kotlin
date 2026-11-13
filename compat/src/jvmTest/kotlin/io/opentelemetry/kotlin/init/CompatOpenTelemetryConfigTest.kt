package io.opentelemetry.kotlin.init

import io.opentelemetry.kotlin.clock.FakeClock
import io.opentelemetry.kotlin.error.FakeSdkErrorHandler
import io.opentelemetry.kotlin.propagation.FakeTextMapPropagator
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

internal class CompatOpenTelemetryConfigTest {

    private val clock = FakeClock()

    @Test
    fun `shutdown timeout accepts positive values and rejects nonpositive values`() {
        val errors = FakeSdkErrorHandler()
        val cfg = CompatOpenTelemetryConfig(clock).apply { errorHandler(errors) }
        assertEquals(5000L, cfg.shutdownTimeoutMs)
        cfg.shutdownTimeout(6000)
        assertEquals(6000L, cfg.shutdownTimeoutMs)
        assertEquals(0, errors.apiMisuses.size)
        listOf(0L, -1L).forEach { timeout ->
            cfg.shutdownTimeout(timeout)
            assertEquals(6000L, cfg.shutdownTimeoutMs)
        }
        assertEquals(2, errors.apiMisuses.size)
    }

    @Test
    fun `propagator uses the supplied custom propagator`() {
        val custom = FakeTextMapPropagator()
        val cfg = CompatOpenTelemetryConfig(clock)
        cfg.propagator { custom }
        assertSame(custom, cfg.createPropagator())
    }

    @Test
    fun `propagator supplier is invoked lazily`() {
        var calls = 0
        val cfg = CompatOpenTelemetryConfig(clock)
        cfg.propagator {
            calls++
            FakeTextMapPropagator()
        }
        assertEquals(0, calls)
        cfg.createPropagator()
        assertEquals(1, calls)
    }

    @Test
    fun `throwing propagator supplier falls back to noop`() {
        val errors = FakeSdkErrorHandler()
        val cfg = CompatOpenTelemetryConfig(clock).apply { errorHandler(errors) }
        cfg.propagator { error("boom") }
        assertEquals(emptyList(), cfg.createPropagator().fields().toList())
        assertEquals(1, errors.userCodeErrors.size)
    }

    @Test
    fun `propagator defaults to noop`() {
        assertEquals(emptyList(), CompatOpenTelemetryConfig(clock).createPropagator().fields().toList())
    }

    @Test
    fun `span limits dsl is reflected in resolved behavior`() {
        val cfg = CompatOpenTelemetryConfig(clock)
        cfg.tracerProvider {
            spanLimits {
                attributeCountLimit = 8
                attributeValueLengthLimit = 16
                linkCountLimit = 32
                eventCountLimit = 64
                attributeCountPerEventLimit = 128
                attributeCountPerLinkLimit = 512
            }
        }

        val behavior = defaultCompatBehaviorReader().read(cfg.configFilePath, cfg::toBehavior)
        val configFactory = CompatSdkConfigFactory(cfg, behavior, clock)
        val spanLimits = configFactory.spanLimits
        assertEquals(8, spanLimits.attributeCountLimit)
        assertEquals(16, spanLimits.attributeValueLengthLimit)
        assertEquals(32, spanLimits.linkCountLimit)
        assertEquals(64, spanLimits.eventCountLimit)
        assertEquals(128, spanLimits.attributeCountPerEventLimit)
        assertEquals(512, spanLimits.attributeCountPerLinkLimit)
    }

    @Test
    fun `log limits dsl is reflected in resolved behavior`() {
        val cfg = CompatOpenTelemetryConfig(clock)
        cfg.loggerProvider {
            logLimits {
                attributeCountLimit = 8
                attributeValueLengthLimit = 16
            }
        }

        val behavior = defaultCompatBehaviorReader().read(cfg.configFilePath, cfg::toBehavior)
        val logLimits = CompatSdkConfigFactory(cfg, behavior, clock).logLimits
        assertEquals(8, logLimits.attributeCountLimit)
        assertEquals(16, logLimits.attributeValueLengthLimit)
    }
}
