package io.opentelemetry.kotlin.init

import io.opentelemetry.kotlin.behavior.SeverityLevel
import io.opentelemetry.kotlin.clock.FakeClock
import io.opentelemetry.kotlin.error.FakeSdkErrorHandler
import io.opentelemetry.kotlin.error.SdkErrorSeverity
import io.opentelemetry.kotlin.logging.SeverityNumber
import io.opentelemetry.kotlin.propagation.FakeTextMapPropagator
import io.opentelemetry.kotlin.propagation.SdkGuardedTextMapPropagator
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
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
    fun `disabled and log level are supplied as behavior`() {
        val cfg = CompatOpenTelemetryConfig()
        cfg.disabled(true)
        cfg.logLevel(SeverityNumber.WARN2)
        val behavior = cfg.toBehavior()
        assertEquals(true, behavior.disabled)
        assertEquals(SeverityLevel.WARN2, behavior.logLevel)
    }

    @Test
    fun `log level drops reports below it`() {
        val errors = FakeSdkErrorHandler()
        val cfg = CompatOpenTelemetryConfig().apply { errorHandler(errors) }
        cfg.logLevel(SeverityNumber.ERROR)
        cfg.shutdownTimeout(-1)
        assertEquals(0, errors.errors.size)

        cfg.minimumErrorSeverity = SdkErrorSeverity.WARNING
        cfg.shutdownTimeout(-1)
        assertEquals(1, errors.apiMisuses.size)
    }

    @Test
    fun `unknown log level is reported and ignored`() {
        val errors = FakeSdkErrorHandler()
        val cfg = CompatOpenTelemetryConfig().apply { errorHandler(errors) }
        cfg.logLevel(SeverityNumber.UNKNOWN)
        assertEquals("OpenTelemetryConfigDsl.logLevel", errors.apiMisuses.single().api)
        assertNull(cfg.toBehavior().logLevel)
    }

    @Test
    fun `propagator uses the supplied custom propagator`() {
        val custom = FakeTextMapPropagator()
        val cfg = CompatOpenTelemetryConfig()
        cfg.propagator { custom }
        assertSame(custom, assertIs<SdkGuardedTextMapPropagator>(cfg.createPropagator()).delegate)
    }

    @Test
    fun `propagator supplier is invoked lazily`() {
        var calls = 0
        val cfg = CompatOpenTelemetryConfig()
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
        val cfg = CompatOpenTelemetryConfig().apply { errorHandler(errors) }
        cfg.propagator { error("boom") }
        assertEquals(emptyList(), cfg.createPropagator().fields().toList())
        assertEquals(1, errors.userCodeErrors.size)
    }

    @Test
    fun `propagator defaults to noop`() {
        assertEquals(emptyList(), CompatOpenTelemetryConfig().createPropagator().fields().toList())
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
        val cfg = CompatOpenTelemetryConfig()
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
