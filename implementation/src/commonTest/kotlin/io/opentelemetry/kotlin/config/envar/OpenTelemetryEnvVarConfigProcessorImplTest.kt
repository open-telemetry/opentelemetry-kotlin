package io.opentelemetry.kotlin.config.envar

import io.opentelemetry.kotlin.clock.FakeClock
import io.opentelemetry.kotlin.config.envar.logging.LogLimitEnvVarConfigProcessorImpl
import io.opentelemetry.kotlin.init.OpenTelemetryConfigImpl
import io.opentelemetry.kotlin.logging.export.FakeLogRecordProcessor
import kotlin.test.Test
import kotlin.test.assertEquals

internal class OpenTelemetryEnvVarConfigProcessorImplTest {

    @Test
    fun `should successfully parse env var value`() {
        //given
        val clock = FakeClock()
        val cfg = OpenTelemetryConfigImpl(clock)
        cfg.loggerProvider {
            export { FakeLogRecordProcessor() }
        }
        val logLimitProcessor = LogLimitEnvVarConfigProcessorImpl(
            envVars = logLimitEnvars()
        )
        val configProcessor = OpenTelemetryEnvVarConfigProcessorImpl(
            loggingConfig = cfg.generateLoggingConfig(),
            logLimitProcessor = logLimitProcessor
        )

        //when
        val environmentConfiguration = configProcessor.process()

        //then
        assertEquals(
            expected = Int.MAX_VALUE,
            actual = environmentConfiguration.logLimitConfig.attributeValueLengthLimit
        )
        assertEquals(
            expected = 128,
            actual = environmentConfiguration.logLimitConfig.attributeCountLimit
        )
    }
}