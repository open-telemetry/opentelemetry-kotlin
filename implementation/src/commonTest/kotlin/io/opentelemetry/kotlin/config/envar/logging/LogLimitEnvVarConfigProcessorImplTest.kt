package io.opentelemetry.kotlin.config.envar.logging

import io.opentelemetry.kotlin.clock.FakeClock
import io.opentelemetry.kotlin.config.envar.EnvVarConstants
import io.opentelemetry.kotlin.init.OpenTelemetryConfigImpl
import io.opentelemetry.kotlin.logging.export.FakeLogRecordProcessor
import kotlin.test.Test
import kotlin.test.assertEquals

internal class LogLimitEnvVarConfigProcessorImplTest {

    @Test
    fun `should successfully configure env var values`() {
        // given
        val processor = LogLimitEnvVarConfigProcessorImpl(
            envVars = EnvVarConstants.LogLimits.envVars
        )
        val clock = FakeClock()
        val otelConfig = OpenTelemetryConfigImpl(clock)
        otelConfig.loggerProvider {
            export { FakeLogRecordProcessor() }
        }
        val defaultValue = otelConfig.generateLoggingConfig().logLimits

        // when
        val config = processor.configure(defaultValue = defaultValue) {
            getFakeEnvVarValue(it)
        }

        // then
        assertEquals(2, config.attributeValueLengthLimit)
        assertEquals(1, config.attributeCountLimit)
    }

    @Test
    fun `should successfully configure config with defaults`() {
        // given
        val processor = LogLimitEnvVarConfigProcessorImpl(
            envVars = EnvVarConstants.LogLimits.envVars
        )
        val clock = FakeClock()
        val otelConfig = OpenTelemetryConfigImpl(clock)
        otelConfig.loggerProvider {
            export { FakeLogRecordProcessor() }
        }
        val defaultValue = otelConfig.generateLoggingConfig().logLimits

        // when
        val config = processor.configure(defaultValue = defaultValue)

        // then
        assertEquals(Int.MAX_VALUE, config.attributeValueLengthLimit)
        assertEquals(128, config.attributeCountLimit)
    }

    @Test
    fun `should successfully configure config with defaults when raw values are null`() {
        // given
        val processor = LogLimitEnvVarConfigProcessorImpl(
            envVars = EnvVarConstants.LogLimits.envVars
        )
        val clock = FakeClock()
        val otelConfig = OpenTelemetryConfigImpl(clock)
        otelConfig.loggerProvider {
            export { FakeLogRecordProcessor() }
        }
        val defaultValue = otelConfig.generateLoggingConfig().logLimits

        // when
        val config = processor.configure(defaultValue = defaultValue) { null }

        // then
        assertEquals(Int.MAX_VALUE, config.attributeValueLengthLimit)
        assertEquals(128, config.attributeCountLimit)
    }

    @Test
    fun `should successfully configure config with defaults when env vars are missing`() {
        // given
        val processor = LogLimitEnvVarConfigProcessorImpl(envVars = emptyList())
        val clock = FakeClock()
        val otelConfig = OpenTelemetryConfigImpl(clock)
        otelConfig.loggerProvider {
            export { FakeLogRecordProcessor() }
        }
        val defaultValue = otelConfig.generateLoggingConfig().logLimits

        // when
        val config = processor.configure(defaultValue = defaultValue)

        // then
        assertEquals(Int.MAX_VALUE, config.attributeValueLengthLimit)
        assertEquals(128, config.attributeCountLimit)
    }

    @Test
    fun `should preserve defaults for invalid environment variables`() {
        val processor = LogLimitEnvVarConfigProcessorImpl(
            envVars = EnvVarConstants.LogLimits.envVars
        )
        val clock = FakeClock()
        val otelConfig = OpenTelemetryConfigImpl(clock)
        otelConfig.loggerProvider {
            export { FakeLogRecordProcessor() }
        }
        val defaultValue = otelConfig.generateLoggingConfig().logLimits

        INVALID_VALUES.forEach { rawValue ->
            val config = processor.configure(defaultValue = defaultValue) { rawValue }

            assertEquals(
                Int.MAX_VALUE,
                config.attributeValueLengthLimit,
                "<$rawValue> should not override the default",
            )
            assertEquals(
                128,
                config.attributeCountLimit,
                "<$rawValue> should not override the default",
            )
        }
    }

    private fun getFakeEnvVarValue(envVar: String): String {
        return when (envVar) {
            "OTEL_LOGRECORD_ATTRIBUTE_COUNT_LIMIT" -> "1"
            "OTEL_LOGRECORD_ATTRIBUTE_VALUE_LENGTH_LIMIT" -> "2"
            else -> "-1"
        }
    }

    private companion object {
        /** Non-numeric, empty, negative, and greater than [Int.MAX_VALUE]. */
        val INVALID_VALUES = listOf("invalid", "", "-1", "2147483648")
    }
}
