package io.opentelemetry.kotlin.init

import io.opentelemetry.kotlin.clock.FakeClock
import org.junit.Test
import kotlin.test.assertEquals

internal class CompatOpenTelemetryConfigTest {

    private val clock = FakeClock()

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

        val behavior = defaultCompatBehaviorReader().read(cfg.configFilePath, cfg.toBehavior())
        val configFactory = CompatSdkConfigFactory(cfg, behavior, clock)
        val spanLimits = configFactory.spanLimits
        assertEquals(8, spanLimits.attributeCountLimit)
        assertEquals(16, spanLimits.attributeValueLengthLimit)
        assertEquals(32, spanLimits.linkCountLimit)
        assertEquals(64, spanLimits.eventCountLimit)
        assertEquals(128, spanLimits.attributeCountPerEventLimit)
        assertEquals(512, spanLimits.attributeCountPerLinkLimit)
    }
}
