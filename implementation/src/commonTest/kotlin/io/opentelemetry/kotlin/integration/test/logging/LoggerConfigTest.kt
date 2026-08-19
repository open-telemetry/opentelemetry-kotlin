package io.opentelemetry.kotlin.integration.test.logging

import io.opentelemetry.kotlin.integration.test.IntegrationTestHarness
import io.opentelemetry.kotlin.logging.LoggerConfigImpl
import io.opentelemetry.kotlin.logging.SeverityNumber
import io.opentelemetry.kotlin.tracing.sampling.alwaysOff
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class LoggerConfigTest {

    private lateinit var harness: IntegrationTestHarness

    @BeforeTest
    fun setUp() = runTest {
        harness = IntegrationTestHarness(testScheduler)
    }

    @Test
    fun testLoggerConfiguratorDisablesMatchingScope() = runTest {
        harness.config.loggerProvider = {
            loggerConfigurator { scope ->
                when (scope.name) {
                    "disabled" -> LoggerConfigImpl(false)
                    else -> LoggerConfigImpl(true)
                }
            }
        }

        val loggerProvider = harness.loggerProvider
        val disabled = loggerProvider.getLogger("disabled")
        val enabled = loggerProvider.getLogger("enabled")

        assertFalse(disabled.enabled())
        assertTrue(enabled.enabled())

        disabled.emit("dropped")
        enabled.emit("kept")

        harness.assertLogRecords(expectedCount = 1) { logs ->
            assertEquals(logs.single().body, "kept")
        }
    }

    @Test
    fun testMinimumSeverityDropsLowSeverityRecords() = runTest {
        harness.config.loggerProvider = {
            loggerConfigurator { LoggerConfigImpl(minimumSeverity = SeverityNumber.WARN) }
        }

        val logger = harness.loggerProvider.getLogger("test")

        assertFalse(logger.enabled(severityNumber = SeverityNumber.INFO))
        assertTrue(logger.enabled(severityNumber = SeverityNumber.WARN))
        assertTrue(logger.enabled())

        logger.emit("dropped", severityNumber = SeverityNumber.INFO)
        logger.emit("kept", severityNumber = SeverityNumber.WARN)
        logger.emit("unspecified")

        harness.assertLogRecords(expectedCount = 2) { logs ->
            assertEquals(listOf("kept", "unspecified"), logs.map { it.body })
        }
    }

    @Test
    fun testTraceBasedDropsRecordsFromUnsampledTraces() = runTest {
        harness.config.tracerProvider = {
            sampler { alwaysOff() }
        }
        harness.config.loggerProvider = {
            loggerConfigurator { LoggerConfigImpl(traceBased = true) }
        }

        val logger = harness.loggerProvider.getLogger("test")
        val unsampled = harness.kotlinApi.context.root()
            .storeSpan(harness.tracer.startSpan("unsampled"))

        assertFalse(logger.enabled(context = unsampled))
        assertTrue(logger.enabled())

        logger.emit("dropped", context = unsampled)
        logger.emit("kept")

        harness.assertLogRecords(expectedCount = 1) { logs ->
            assertEquals("kept", logs.single().body)
        }
    }
}
