package io.opentelemetry.kotlin.config.envar

import io.opentelemetry.kotlin.clock.FakeClock
import io.opentelemetry.kotlin.init.OpenTelemetryConfigImpl
import io.opentelemetry.kotlin.logging.export.FakeLogRecordProcessor
import kotlin.test.Test
import kotlin.test.assertEquals

internal class OpenTelemetryEnvVarConfigProcessorImplTest {

    @Test
    fun `should successfully parse env var value`() {
        // given
        val clock = FakeClock()
        val cfg = OpenTelemetryConfigImpl(clock)
        cfg.loggerProvider {
            export { FakeLogRecordProcessor() }
        }
        val logLimitProcessor = FakeLogLimitEnvVarConfigProcessor()
        val spanLimitProcessor = FakeSpanLimitEnvVarConfigProcessor()
        val configProcessor = OpenTelemetryEnvVarConfigProcessorImpl(
            loggingConfig = cfg.generateLoggingConfig(),
            logLimitProcessor = logLimitProcessor,
            tracingConfig = cfg.generateTracingConfig(),
            spanLimitProcessor = spanLimitProcessor,
        )

        // when
        val environmentConfiguration = configProcessor.process()

        // then
        assertEquals(
            expected = Int.MAX_VALUE,
            actual = environmentConfiguration.logLimitConfig.attributeValueLengthLimit
        )
        assertEquals(
            expected = 128,
            actual = environmentConfiguration.logLimitConfig.attributeCountLimit
        )
        assertEquals(
            expected = Int.MAX_VALUE,
            actual = environmentConfiguration.spanLimitConfig.attributeValueLengthLimit
        )
        assertEquals(
            expected = 128,
            actual = environmentConfiguration.spanLimitConfig.attributeCountLimit
        )
        assertEquals(
            expected = 128,
            actual = environmentConfiguration.spanLimitConfig.eventCountLimit
        )
        assertEquals(
            expected = 128,
            actual = environmentConfiguration.spanLimitConfig.linkCountLimit
        )
        assertEquals(
            expected = 128,
            actual = environmentConfiguration.spanLimitConfig.attributeCountPerEventLimit
        )
        assertEquals(
            expected = 128,
            actual = environmentConfiguration.spanLimitConfig.attributeCountPerLinkLimit
        )
    }
}
