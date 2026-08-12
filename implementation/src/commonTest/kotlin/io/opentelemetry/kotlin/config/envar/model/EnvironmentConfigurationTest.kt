package io.opentelemetry.kotlin.config.envar.model

import io.opentelemetry.kotlin.clock.FakeClock
import io.opentelemetry.kotlin.init.OpenTelemetryConfigImpl
import io.opentelemetry.kotlin.logging.export.FakeLogRecordProcessor
import kotlin.test.Test
import kotlin.test.assertEquals

internal class EnvironmentConfigurationTest {
    @Test
    fun `should successfully create an environment configuration`() {
        // given
        val clock = FakeClock()
        val otelConfig = OpenTelemetryConfigImpl(clock)
        otelConfig.loggerProvider {
            export { FakeLogRecordProcessor() }
        }
        val defaultLogLimits = otelConfig.generateLoggingConfig().logLimits
        val defaultSpanLimits = otelConfig.generateTracingConfig().spanLimits

        // when
        val config = EnvironmentConfiguration(
            logLimitConfig = defaultLogLimits,
            spanLimitConfig = defaultSpanLimits,
        )

        // then
        assertEquals(Int.MAX_VALUE, config.logLimitConfig.attributeValueLengthLimit)
        assertEquals(128, config.logLimitConfig.attributeCountLimit)
        assertEquals(Int.MAX_VALUE, config.spanLimitConfig.attributeValueLengthLimit)
        assertEquals(128, config.spanLimitConfig.attributeCountLimit)
        assertEquals(128, config.spanLimitConfig.eventCountLimit)
        assertEquals(128, config.spanLimitConfig.linkCountLimit)
        assertEquals(128, config.spanLimitConfig.attributeCountPerEventLimit)
        assertEquals(128, config.spanLimitConfig.attributeCountPerLinkLimit)
    }
}
