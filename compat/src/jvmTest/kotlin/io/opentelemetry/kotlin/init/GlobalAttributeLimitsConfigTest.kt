package io.opentelemetry.kotlin.init

import io.opentelemetry.kotlin.clock.FakeClock
import io.opentelemetry.kotlin.factory.CompatIdGenerator
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class GlobalAttributeLimitsConfigTest {

    private val clock = FakeClock()

    @Test
    fun `CompatAttributeLimitsConfig default state`() {
        val cfg = CompatAttributeLimitsConfig()
        assertFalse(cfg.attributeCountLimitSet)
        assertFalse(cfg.attributeValueLengthLimitSet)
        assertEquals(DEFAULT_ATTR_LIMIT, cfg.attributeCountLimit)
        assertEquals(DEFAULT_ATTR_VALUE_LENGTH_LIMIT, cfg.attributeValueLengthLimit)
    }

    @Test
    fun `CompatAttributeLimitsConfig sets flags on assignment`() {
        val cfg = CompatAttributeLimitsConfig()
        cfg.attributeCountLimit = 64
        cfg.attributeValueLengthLimit = 256
        assertTrue(cfg.attributeCountLimitSet)
        assertTrue(cfg.attributeValueLengthLimitSet)
        assertEquals(64, cfg.attributeCountLimit)
        assertEquals(256, cfg.attributeValueLengthLimit)
    }

    @Test
    fun `global only - applies to spans and logs`() {
        val globalLimits = CompatAttributeLimitsConfig().apply { attributeCountLimit = 64 }

        val tracerConfig = CompatTracerProviderConfig(clock, CompatIdGenerator())
        tracerConfig.build(clock, globalLimits = globalLimits)
        assertEquals(64, tracerConfig.spanLimitsConfig.attributeCountLimit)

        val loggerConfig = CompatLoggerProviderConfig(clock)
        loggerConfig.build(clock, globalLimits = globalLimits)
        assertEquals(64, loggerConfig.logLimitsConfig.attributeCountLimit)
    }

    @Test
    fun `signal-specific overrides global`() {
        val globalLimits = CompatAttributeLimitsConfig().apply { attributeCountLimit = 64 }

        val tracerConfig = CompatTracerProviderConfig(clock, CompatIdGenerator()).apply {
            spanLimits { attributeCountLimit = 32 }
        }
        tracerConfig.build(clock, globalLimits = globalLimits)
        assertEquals(32, tracerConfig.spanLimitsConfig.attributeCountLimit)

        val loggerConfig = CompatLoggerProviderConfig(clock)
        loggerConfig.build(clock, globalLimits = globalLimits)
        assertEquals(64, loggerConfig.logLimitsConfig.attributeCountLimit)
    }

    @Test
    fun `partial signal override - other global properties still apply`() {
        val globalLimits = CompatAttributeLimitsConfig().apply { attributeCountLimit = 64 }

        val tracerConfig = CompatTracerProviderConfig(clock, CompatIdGenerator()).apply {
            spanLimits { attributeValueLengthLimit = 256 }
        }
        tracerConfig.build(clock, globalLimits = globalLimits)
        assertEquals(64, tracerConfig.spanLimitsConfig.attributeCountLimit)
        assertEquals(256, tracerConfig.spanLimitsConfig.attributeValueLengthLimit)
    }

    @Test
    fun `no global - defaults are zero (Java SDK uses its own defaults)`() {
        val tracerConfig = CompatTracerProviderConfig(clock, CompatIdGenerator())
        tracerConfig.build(clock)
        assertEquals(DEFAULT_ATTR_LIMIT, tracerConfig.spanLimitsConfig.attributeCountLimit)
        assertEquals(DEFAULT_ATTR_VALUE_LENGTH_LIMIT, tracerConfig.spanLimitsConfig.attributeValueLengthLimit)

        val loggerConfig = CompatLoggerProviderConfig(clock)
        loggerConfig.build(clock)
        assertEquals(DEFAULT_ATTR_LIMIT, loggerConfig.logLimitsConfig.attributeCountLimit)
        assertEquals(DEFAULT_ATTR_VALUE_LENGTH_LIMIT, loggerConfig.logLimitsConfig.attributeValueLengthLimit)
    }
}
