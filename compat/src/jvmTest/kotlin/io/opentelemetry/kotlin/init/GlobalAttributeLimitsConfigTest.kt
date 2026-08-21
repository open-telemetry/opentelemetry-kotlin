package io.opentelemetry.kotlin.init

import io.opentelemetry.kotlin.aliases.OtelJavaLogLimits
import io.opentelemetry.kotlin.aliases.OtelJavaSpanLimits
import io.opentelemetry.kotlin.clock.FakeClock
import io.opentelemetry.kotlin.error.NoopSdkErrorHandler
import io.opentelemetry.kotlin.factory.CompatIdGenerator
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class GlobalAttributeLimitsConfigTest {

    private val clock = FakeClock()
    private val idGenerator = CompatIdGenerator()

    @Test
    fun `CompatAttributeLimitsConfig default state`() {
        val cfg = CompatAttributeLimitsConfig()
        assertNull(cfg.attributeCountLimit)
        assertNull(cfg.attributeValueLengthLimit)
    }

    @Test
    fun `global only - applies to spans and logs`() {
        val globalLimits = CompatAttributeLimitsConfig().apply { attributeCountLimit = 64 }

        val tracerConfig = CompatTracerProviderConfig(clock, NoopSdkErrorHandler)
        tracerConfig.build(clock, idGenerator, globalLimits = globalLimits)
        assertEquals(64, tracerConfig.spanLimitsConfig.attributeCountLimit)

        val loggerConfig = CompatLoggerProviderConfig(clock, NoopSdkErrorHandler)
        loggerConfig.build(clock, globalLimits = globalLimits)
        assertEquals(64, loggerConfig.logLimitsConfig.attributeCountLimit)
    }

    @Test
    fun `signal-specific overrides global`() {
        val globalLimits = CompatAttributeLimitsConfig().apply { attributeCountLimit = 64 }

        val tracerConfig = CompatTracerProviderConfig(clock, NoopSdkErrorHandler).apply {
            spanLimits { attributeCountLimit = 32 }
        }
        tracerConfig.build(clock, idGenerator, globalLimits = globalLimits)
        assertEquals(32, tracerConfig.spanLimitsConfig.attributeCountLimit)

        val loggerConfig = CompatLoggerProviderConfig(clock, NoopSdkErrorHandler)
        loggerConfig.build(clock, globalLimits = globalLimits)
        assertEquals(64, loggerConfig.logLimitsConfig.attributeCountLimit)
    }

    @Test
    fun `a signal-specific zero is not treated as unset`() {
        val globalLimits = CompatAttributeLimitsConfig().apply { attributeCountLimit = 64 }

        val tracerConfig = CompatTracerProviderConfig(clock, NoopSdkErrorHandler).apply {
            spanLimits { attributeCountLimit = 0 }
        }
        tracerConfig.build(clock, idGenerator, globalLimits = globalLimits)
        assertEquals(0, tracerConfig.spanLimitsConfig.attributeCountLimit)

        val loggerConfig = CompatLoggerProviderConfig(clock, NoopSdkErrorHandler).apply {
            logLimits { attributeCountLimit = 0 }
        }
        loggerConfig.build(clock, globalLimits = globalLimits)
        assertEquals(0, loggerConfig.logLimitsConfig.attributeCountLimit)
    }

    @Test
    fun `partial signal override - other global properties still apply`() {
        val globalLimits = CompatAttributeLimitsConfig().apply { attributeCountLimit = 64 }

        val tracerConfig = CompatTracerProviderConfig(clock, NoopSdkErrorHandler).apply {
            spanLimits { attributeValueLengthLimit = 256 }
        }
        tracerConfig.build(clock, idGenerator, globalLimits = globalLimits)
        assertEquals(64, tracerConfig.spanLimitsConfig.attributeCountLimit)
        assertEquals(256, tracerConfig.spanLimitsConfig.attributeValueLengthLimit)
    }

    @Test
    fun `no global - limits stay unset so the Java SDK applies its own defaults`() {
        val tracerConfig = CompatTracerProviderConfig(clock, NoopSdkErrorHandler)
        tracerConfig.build(clock, idGenerator)
        assertNull(tracerConfig.spanLimitsConfig.attributeCountLimit)
        assertNull(tracerConfig.spanLimitsConfig.attributeValueLengthLimit)
        assertEquals(OtelJavaSpanLimits.getDefault(), tracerConfig.spanLimitsConfig.build())

        val loggerConfig = CompatLoggerProviderConfig(clock, NoopSdkErrorHandler)
        loggerConfig.build(clock)
        assertNull(loggerConfig.logLimitsConfig.attributeCountLimit)
        assertNull(loggerConfig.logLimitsConfig.attributeValueLengthLimit)
        assertEquals(OtelJavaLogLimits.getDefault(), loggerConfig.logLimitsConfig.build())
    }
}
