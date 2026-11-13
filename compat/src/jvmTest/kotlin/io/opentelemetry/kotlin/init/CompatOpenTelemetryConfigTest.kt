package io.opentelemetry.kotlin.init

import io.opentelemetry.kotlin.clock.FakeClock
import io.opentelemetry.kotlin.error.FakeSdkErrorHandler
import io.opentelemetry.kotlin.factory.CompatContextFactory
import io.opentelemetry.kotlin.propagation.FakeTextMapPropagator
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

internal class CompatOpenTelemetryConfigTest {

    private val clock = FakeClock()

    @Test
    fun `shutdown timeout accepts positive values and rejects nonpositive values`() {
        val errors = FakeSdkErrorHandler()
        val cfg = CompatOpenTelemetryConfig().apply { errorHandler(errors) }
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
    fun `propagator dsl uses a custom propagator returned from the lambda`() {
        val custom = FakeTextMapPropagator()
        val cfg = CompatOpenTelemetryConfig()
        cfg.propagator { custom }
        assertSame(custom, cfg.propagator)
    }

    @Test
    fun `propagator dsl uses the lambda return value rather than the last builder call`() {
        val cfg = CompatOpenTelemetryConfig()
        cfg.propagator {
            val traceContext = w3cTraceContext()
            w3cBaggage()
            traceContext
        }
        assertEquals(listOf("traceparent", "tracestate"), cfg.propagator.fields().toList())
    }

    @Test
    fun `propagator defaults to noop`() {
        assertEquals(emptyList(), CompatOpenTelemetryConfig().propagator.fields().toList())
    }

    @Test
    fun `span limits dsl is reflected in resolved behavior`() {
        val cfg = CompatOpenTelemetryConfig()
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
        val configFactory = CompatSdkConfigFactory(cfg, behavior, clock, CompatContextFactory())
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
        val cfg = CompatOpenTelemetryConfig()
        cfg.loggerProvider {
            logLimits {
                attributeCountLimit = 8
                attributeValueLengthLimit = 16
            }
        }

        val behavior = defaultCompatBehaviorReader().read(cfg.configFilePath, cfg::toBehavior)
        val logLimits = CompatSdkConfigFactory(cfg, behavior, clock, CompatContextFactory()).logLimits
        assertEquals(8, logLimits.attributeCountLimit)
        assertEquals(16, logLimits.attributeValueLengthLimit)
    }
}
