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
        val defaultValue = otelConfig.generateLoggingConfig().logLimits

        // when
        val config = EnvironmentConfiguration(
            logLimitConfig = defaultValue
        )

        // then
        assertEquals(Int.MAX_VALUE, config.logLimitConfig.attributeValueLengthLimit)
        assertEquals(128, config.logLimitConfig.attributeCountLimit)
    }
}
