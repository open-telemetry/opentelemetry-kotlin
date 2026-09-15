package io.opentelemetry.kotlin.init

import io.opentelemetry.kotlin.aliases.OtelJavaLogLimits
import io.opentelemetry.kotlin.aliases.OtelJavaSpanLimits
import io.opentelemetry.kotlin.behavior.AttributeLimitsBehavior
import io.opentelemetry.kotlin.behavior.LogLimitsBehavior
import io.opentelemetry.kotlin.behavior.SpanLimitsBehavior
import io.opentelemetry.kotlin.clock.FakeClock
import io.opentelemetry.kotlin.config.dsl.AttributeLimitsConfigDslImpl
import io.opentelemetry.kotlin.error.NoopSdkErrorHandler
import io.opentelemetry.kotlin.factory.CompatIdGenerator
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class GlobalAttributeLimitsConfigTest {

    private val clock = FakeClock()
    private val idGenerator = CompatIdGenerator()
    private val noGlobalLimits = AttributeLimitsBehavior()
    private val noSpanLimits = SpanLimitsBehavior()
    private val noLogLimits = LogLimitsBehavior()

    @Test
    fun `global only - applies to spans and logs`() {
        val globalLimits = AttributeLimitsBehavior(attributeCountLimit = 64)

        val tracerConfig = CompatTracerProviderConfig(clock, NoopSdkErrorHandler)
        tracerConfig.build(clock, idGenerator, globalLimits = globalLimits, spanLimits = noSpanLimits)
        assertEquals(64, tracerConfig.spanLimitsConfig.attributeCountLimit)

        val loggerConfig = CompatLoggerProviderConfig(clock, NoopSdkErrorHandler)
        loggerConfig.build(clock, globalLimits = globalLimits, logLimits = noLogLimits)
        assertEquals(64, loggerConfig.logLimits.attributeCountLimit)
    }

    @Test
    fun `signal-specific overrides global`() {
        val globalLimits = AttributeLimitsBehavior(attributeCountLimit = 64)
        val spanLimits = SpanLimitsBehavior(attributeCountLimit = 32)
        val logLimits = LogLimitsBehavior(attributeCountLimit = 16)

        val tracerConfig = CompatTracerProviderConfig(clock, NoopSdkErrorHandler)
        tracerConfig.build(clock, idGenerator, globalLimits = globalLimits, spanLimits = spanLimits)
        assertEquals(32, tracerConfig.spanLimitsConfig.attributeCountLimit)

        val loggerConfig = CompatLoggerProviderConfig(clock, NoopSdkErrorHandler)
        loggerConfig.build(clock, globalLimits = globalLimits, logLimits = logLimits)
        assertEquals(16, loggerConfig.logLimits.attributeCountLimit)
    }

    @Test
    fun `a signal-specific zero is not treated as unset`() {
        val globalLimits = AttributeLimitsBehavior(attributeCountLimit = 64)
        val spanLimits = SpanLimitsBehavior(attributeCountLimit = 0)
        val logLimits = LogLimitsBehavior(attributeCountLimit = 0)

        val tracerConfig = CompatTracerProviderConfig(clock, NoopSdkErrorHandler)
        tracerConfig.build(clock, idGenerator, globalLimits = globalLimits, spanLimits = spanLimits)
        assertEquals(0, tracerConfig.spanLimitsConfig.attributeCountLimit)

        val loggerConfig = CompatLoggerProviderConfig(clock, NoopSdkErrorHandler)
        loggerConfig.build(clock, globalLimits = globalLimits, logLimits = logLimits)
        assertEquals(0, loggerConfig.logLimits.attributeCountLimit)
    }

    @Test
    fun `partial signal override - other global properties still apply`() {
        val globalLimits = AttributeLimitsBehavior(attributeCountLimit = 64)
        val spanLimits = SpanLimitsBehavior(attributeValueLengthLimit = 256)
        val logLimits = LogLimitsBehavior(attributeValueLengthLimit = 256)

        val tracerConfig = CompatTracerProviderConfig(clock, NoopSdkErrorHandler)
        tracerConfig.build(clock, idGenerator, globalLimits = globalLimits, spanLimits = spanLimits)
        assertEquals(64, tracerConfig.spanLimitsConfig.attributeCountLimit)
        assertEquals(256, tracerConfig.spanLimitsConfig.attributeValueLengthLimit)

        val loggerConfig = CompatLoggerProviderConfig(clock, NoopSdkErrorHandler)
        loggerConfig.build(clock, globalLimits = globalLimits, logLimits = logLimits)
        assertEquals(64, loggerConfig.logLimits.attributeCountLimit)
        assertEquals(256, loggerConfig.logLimits.attributeValueLengthLimit)
    }

    @Test
    fun `no global - limits stay unset so the Java SDK applies its own defaults`() {
        val tracerConfig = CompatTracerProviderConfig(clock, NoopSdkErrorHandler)
        tracerConfig.build(clock, idGenerator, globalLimits = noGlobalLimits, spanLimits = noSpanLimits)
        assertNull(tracerConfig.spanLimitsConfig.attributeCountLimit)
        assertNull(tracerConfig.spanLimitsConfig.attributeValueLengthLimit)
        assertEquals(OtelJavaSpanLimits.getDefault(), tracerConfig.spanLimitsConfig.build())

        val loggerConfig = CompatLoggerProviderConfig(clock, NoopSdkErrorHandler)
        loggerConfig.build(clock, globalLimits = noGlobalLimits, logLimits = noLogLimits)
        assertNull(loggerConfig.logLimits.attributeCountLimit)
        assertNull(loggerConfig.logLimits.attributeValueLengthLimit)
        assertEquals(OtelJavaLogLimits.getDefault(), loggerConfig.logLimits.toOtelJavaLogLimits())
    }

    @Test
    fun `the merged global limit reaches the adapters`() {
        val globalLimits = AttributeLimitsBehavior(attributeCountLimit = 64)

        val tracerConfig = CompatTracerProviderConfig(clock, NoopSdkErrorHandler)
        tracerConfig.build(clock, idGenerator, globalLimits = globalLimits, spanLimits = noSpanLimits)
        assertEquals(64, tracerConfig.spanLimitsConfig.effectiveAttributeCountLimit)
    }

    @Test
    fun `a negative global limit is treated as unset`() {
        val globalLimits = AttributeLimitsConfigDslImpl().apply {
            attributeCountLimit = -1
            attributeValueLengthLimit = -1
        }.toBehavior()

        val tracerConfig = CompatTracerProviderConfig(clock, NoopSdkErrorHandler)
        tracerConfig.build(clock, idGenerator, globalLimits = globalLimits, spanLimits = noSpanLimits)
        assertNull(tracerConfig.spanLimitsConfig.attributeCountLimit)
        assertNull(tracerConfig.spanLimitsConfig.attributeValueLengthLimit)
        assertEquals(OtelJavaSpanLimits.getDefault(), tracerConfig.spanLimitsConfig.build())
    }
}
