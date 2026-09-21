package io.opentelemetry.kotlin.init

import io.opentelemetry.kotlin.aliases.OtelJavaLogLimits
import io.opentelemetry.kotlin.aliases.OtelJavaSpanLimits
import io.opentelemetry.kotlin.behavior.AttributeLimitsBehavior
import io.opentelemetry.kotlin.behavior.BehaviorResolverImpl
import io.opentelemetry.kotlin.behavior.LogLimitsBehavior
import io.opentelemetry.kotlin.behavior.LoggerProviderBehavior
import io.opentelemetry.kotlin.behavior.OpenTelemetryBehavior
import io.opentelemetry.kotlin.behavior.SpanLimitsBehavior
import io.opentelemetry.kotlin.behavior.TracerProviderBehavior
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

    @Test
    fun `global only - applies to spans and logs`() {
        val global = AttributeLimitsBehavior(attributeCountLimit = 64)

        assertEquals(64, buildSpanLimits(global).attributeCountLimit)
        assertEquals(64, buildLogLimits(global).attributeCountLimit)
    }

    @Test
    fun `signal-specific overrides global`() {
        val global = AttributeLimitsBehavior(attributeCountLimit = 64)

        val spanLimits = buildSpanLimits(global, SpanLimitsBehavior(attributeCountLimit = 32))
        assertEquals(32, spanLimits.attributeCountLimit)

        val logLimits = buildLogLimits(global, LogLimitsBehavior(attributeCountLimit = 16))
        assertEquals(16, logLimits.attributeCountLimit)
    }

    @Test
    fun `a signal-specific zero is not treated as unset`() {
        val global = AttributeLimitsBehavior(attributeCountLimit = 64)

        assertEquals(0, buildSpanLimits(global, SpanLimitsBehavior(attributeCountLimit = 0)).attributeCountLimit)
        assertEquals(0, buildLogLimits(global, LogLimitsBehavior(attributeCountLimit = 0)).attributeCountLimit)
    }

    @Test
    fun `partial signal override - other global properties still apply`() {
        val global = AttributeLimitsBehavior(attributeCountLimit = 64)

        val spanLimits = buildSpanLimits(global, SpanLimitsBehavior(attributeValueLengthLimit = 256))
        assertEquals(64, spanLimits.attributeCountLimit)
        assertEquals(256, spanLimits.attributeValueLengthLimit)

        val logLimits = buildLogLimits(global, LogLimitsBehavior(attributeValueLengthLimit = 256))
        assertEquals(64, logLimits.attributeCountLimit)
        assertEquals(256, logLimits.attributeValueLengthLimit)
    }

    @Test
    fun `no global - limits stay unset so the Java SDK applies its own defaults`() {
        val spanLimits = buildSpanLimits()
        assertNull(spanLimits.attributeCountLimit)
        assertNull(spanLimits.attributeValueLengthLimit)
        assertEquals(OtelJavaSpanLimits.getDefault(), spanLimits.build())

        val logLimits = buildLogLimits()
        assertNull(logLimits.attributeCountLimit)
        assertNull(logLimits.attributeValueLengthLimit)
        assertEquals(OtelJavaLogLimits.getDefault(), logLimits.toOtelJavaLogLimits())
    }

    @Test
    fun `the merged global limit reaches the adapters`() {
        val global = AttributeLimitsBehavior(attributeCountLimit = 64)

        assertEquals(64, buildSpanLimits(global).effectiveAttributeCountLimit)
    }

    @Test
    fun `a negative global limit is treated as unset`() {
        val global = AttributeLimitsConfigDslImpl().apply {
            attributeCountLimit = -1
            attributeValueLengthLimit = -1
        }.toBehavior()

        val spanLimits = buildSpanLimits(global)
        assertNull(spanLimits.attributeCountLimit)
        assertNull(spanLimits.attributeValueLengthLimit)
        assertEquals(OtelJavaSpanLimits.getDefault(), spanLimits.build())
    }

    private fun buildSpanLimits(
        global: AttributeLimitsBehavior? = null,
        spanLimits: SpanLimitsBehavior? = null,
    ): CompatSpanLimitsConfig {
        val resolved = resolve(global, spanLimits = spanLimits)
        val config = CompatTracerProviderConfig(clock, NoopSdkErrorHandler)
        config.build(
            clock,
            idGenerator,
            spanLimits = resolved.tracerProvider?.spanLimits ?: SpanLimitsBehavior(),
        )
        return config.spanLimitsConfig
    }

    private fun buildLogLimits(
        global: AttributeLimitsBehavior? = null,
        logLimits: LogLimitsBehavior? = null,
    ): AttributeLimitsBehavior {
        val resolved = resolve(global, logLimits = logLimits)
        val config = CompatLoggerProviderConfig(clock, NoopSdkErrorHandler)
        config.build(clock, logLimits = resolved.loggerProvider?.logLimits ?: LogLimitsBehavior())
        return config.logLimits
    }

    private fun resolve(
        global: AttributeLimitsBehavior? = null,
        spanLimits: SpanLimitsBehavior? = null,
        logLimits: LogLimitsBehavior? = null,
    ): OpenTelemetryBehavior = BehaviorResolverImpl().resolve(
        envars = null,
        declarativeFile = null,
        dsl = OpenTelemetryBehavior(
            attributeLimits = global,
            tracerProvider = spanLimits?.let { TracerProviderBehavior(spanLimits = it) },
            loggerProvider = logLimits?.let { LoggerProviderBehavior(logLimits = it) },
        ),
    )
}
