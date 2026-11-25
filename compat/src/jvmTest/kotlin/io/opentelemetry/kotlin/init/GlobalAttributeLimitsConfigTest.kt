package io.opentelemetry.kotlin.init

import io.opentelemetry.kotlin.aliases.OtelJavaLogLimits
import io.opentelemetry.kotlin.aliases.OtelJavaSpanLimits
import io.opentelemetry.kotlin.behavior.AttributeLimitsBehavior
import io.opentelemetry.kotlin.behavior.BehaviorResolverImpl
import io.opentelemetry.kotlin.behavior.OpenTelemetryBehavior
import io.opentelemetry.kotlin.behavior.logging.LogRecordLimitsBehavior
import io.opentelemetry.kotlin.behavior.logging.LoggerProviderBehavior
import io.opentelemetry.kotlin.behavior.tracing.SpanLimitsBehavior
import io.opentelemetry.kotlin.behavior.tracing.TracerProviderBehavior
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
        assertEquals(64, buildLogRecordLimits(global).attributeCountLimit)
    }

    @Test
    fun `signal-specific overrides global`() {
        val global = AttributeLimitsBehavior(attributeCountLimit = 64)

        val spanLimits = buildSpanLimits(global, SpanLimitsBehavior(attributeCountLimit = 32))
        assertEquals(32, spanLimits.attributeCountLimit)

        val logRecordLimits = buildLogRecordLimits(global, LogRecordLimitsBehavior(attributeCountLimit = 16))
        assertEquals(16, logRecordLimits.attributeCountLimit)
    }

    @Test
    fun `a signal-specific zero is not treated as unset`() {
        val global = AttributeLimitsBehavior(attributeCountLimit = 64)

        assertEquals(0, buildSpanLimits(global, SpanLimitsBehavior(attributeCountLimit = 0)).attributeCountLimit)
        assertEquals(0, buildLogRecordLimits(global, LogRecordLimitsBehavior(attributeCountLimit = 0)).attributeCountLimit)
    }

    @Test
    fun `partial signal override - other global properties still apply`() {
        val global = AttributeLimitsBehavior(attributeCountLimit = 64)

        val spanLimits = buildSpanLimits(global, SpanLimitsBehavior(attributeValueLengthLimit = 256))
        assertEquals(64, spanLimits.attributeCountLimit)
        assertEquals(256, spanLimits.attributeValueLengthLimit)

        val logRecordLimits = buildLogRecordLimits(global, LogRecordLimitsBehavior(attributeValueLengthLimit = 256))
        assertEquals(64, logRecordLimits.attributeCountLimit)
        assertEquals(256, logRecordLimits.attributeValueLengthLimit)
    }

    @Test
    fun `no global - limits stay unset so the Java SDK applies its own defaults`() {
        val spanLimits = buildSpanLimits()
        assertNull(spanLimits.attributeCountLimit)
        assertNull(spanLimits.attributeValueLengthLimit)
        assertEquals(OtelJavaSpanLimits.getDefault(), spanLimits.build())

        val logRecordLimits = buildLogRecordLimits()
        assertNull(logRecordLimits.attributeCountLimit)
        assertNull(logRecordLimits.attributeValueLengthLimit)
        assertEquals(OtelJavaLogLimits.getDefault(), logRecordLimits.toOtelJavaLogLimits())
    }

    @Test
    fun `the merged global limit reaches the Java span limits`() {
        val global = AttributeLimitsBehavior(attributeCountLimit = 64)

        assertEquals(64, buildSpanLimits(global).build().maxNumberOfAttributes)
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

    private fun buildLogRecordLimits(
        global: AttributeLimitsBehavior? = null,
        logRecordLimits: LogRecordLimitsBehavior? = null,
    ): AttributeLimitsBehavior {
        val resolved = resolve(global, logRecordLimits = logRecordLimits)
        val config = CompatLoggerProviderConfig(clock, NoopSdkErrorHandler)
        config.build(clock, logRecordLimits = resolved.loggerProvider?.logRecordLimits ?: LogRecordLimitsBehavior())
        return config.logRecordLimits
    }

    private fun resolve(
        global: AttributeLimitsBehavior? = null,
        spanLimits: SpanLimitsBehavior? = null,
        logRecordLimits: LogRecordLimitsBehavior? = null,
    ): OpenTelemetryBehavior = BehaviorResolverImpl().resolve(
        envars = null,
        declarativeFile = null,
        dsl = OpenTelemetryBehavior(
            attributeLimits = global,
            tracerProvider = spanLimits?.let { TracerProviderBehavior(spanLimits = it) },
            loggerProvider = logRecordLimits?.let { LoggerProviderBehavior(logRecordLimits = it) },
        ),
    )
}
