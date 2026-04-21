package io.opentelemetry.kotlin.config.envar.logging

import io.opentelemetry.kotlin.clock.FakeClock
import io.opentelemetry.kotlin.config.envar.logLimitEnvars
import io.opentelemetry.kotlin.init.OpenTelemetryConfigImpl
import io.opentelemetry.kotlin.logging.export.FakeLogRecordProcessor
import kotlin.test.Test
import kotlin.test.assertEquals

internal class LogLimitEnvVarConfigProcessorImplTest {

    @Test
    fun `should successfully parse env var value`() {
        //given
        val processor = LogLimitEnvVarConfigProcessorImpl(
            envVars = logLimitEnvars()
        )

        //when
        val config = processor.resolve { "1" }

        //then
        assertEquals(1, config.attributeValueLengthLimit)
        assertEquals(1, config.attributeCountLimit)
    }

    @Test
    fun `should successfully process env var value`() {
        //given
        val processor = LogLimitEnvVarConfigProcessorImpl(
            envVars = logLimitEnvars()
        )
        val clock = FakeClock()
        val otelConfig = OpenTelemetryConfigImpl(clock)
        otelConfig.loggerProvider {
            export { FakeLogRecordProcessor() }
        }
        val defaultValue = otelConfig.generateLoggingConfig().logLimits

        //when
        val config = processor.resolve(defaultValue = defaultValue)

        //then
        assertEquals(Int.MAX_VALUE, config.attributeValueLengthLimit)
        assertEquals(128, config.attributeCountLimit)
    }
}