package io.opentelemetry.kotlin.logging

import io.opentelemetry.kotlin.framework.OtelKotlinHarness
import io.opentelemetry.kotlin.tracing.sampling.alwaysOff
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class LoggerConfigTest {

    private lateinit var harness: OtelKotlinHarness

    @BeforeTest
    fun setUp() = runTest {
        harness = OtelKotlinHarness(testScheduler)
    }

    @Test
    fun `loggerConfigurator disables matching scope`() = runTest {
        harness.config.loggerProvider = {
            loggerConfigurator { scope ->
                object : LoggerConfig {
                    override val enabled = scope.name != "disabled"
                }
            }
        }

        val loggerProvider = harness.kotlinApi.loggerProvider
        val disabled = loggerProvider.getLogger("disabled")
        val enabled = loggerProvider.getLogger("enabled")

        assertFalse(disabled.enabled())
        assertTrue(enabled.enabled())

        disabled.emit("dropped")
        enabled.emit("kept")

        harness.assertLogRecords(expectedCount = 1) { logs ->
            assertEquals("kept", logs.single().body)
        }
    }

    @Test
    fun `minimumSeverity drops low severity records`() = runTest {
        harness.config.loggerProvider = {
            loggerConfigurator {
                object : LoggerConfig {
                    override val minimumSeverity = SeverityNumber.WARN
                }
            }
        }

        val logger = harness.kotlinApi.loggerProvider.getLogger("test")

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
    fun `traceBased drops records from unsampled traces`() = runTest {
        harness.config.tracerProvider = {
            sampler { alwaysOff() }
        }
        harness.config.loggerProvider = {
            loggerConfigurator {
                object : LoggerConfig {
                    override val traceBased = true
                }
            }
        }

        val logger = harness.kotlinApi.loggerProvider.getLogger("test")
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
