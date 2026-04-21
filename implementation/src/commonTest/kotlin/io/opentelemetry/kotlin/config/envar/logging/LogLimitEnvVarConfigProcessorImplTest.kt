package io.opentelemetry.kotlin.config.envar.logging

import io.opentelemetry.kotlin.clock.FakeClock
import io.opentelemetry.kotlin.config.envar.logLimitEnvars
import io.opentelemetry.kotlin.init.OpenTelemetryConfigImpl
import io.opentelemetry.kotlin.logging.export.FakeLogRecordProcessor
import kotlin.test.Test
import kotlin.test.assertEquals

internal class LogLimitEnvVarConfigProcessorImplTest {

    @Test
    fun `should successfully resolve env var values`() {
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
        val config = processor.resolve(defaultValue = defaultValue) {
            getFakeEnvVarValue(it)
        }

        //then
        assertEquals(2, config.attributeValueLengthLimit)
        assertEquals(1, config.attributeCountLimit)
    }

    @Test
    fun `should successfully resolve config with defaults`() {
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

    private fun getFakeEnvVarValue(envVar: String): String {
        return when (envVar) {
            "OTEL_LOGRECORD_ATTRIBUTE_COUNT_LIMIT" -> "1"
            "OTEL_LOGRECORD_ATTRIBUTE_VALUE_LENGTH_LIMIT" -> "2"
            else -> "-1"
        }
    }
}