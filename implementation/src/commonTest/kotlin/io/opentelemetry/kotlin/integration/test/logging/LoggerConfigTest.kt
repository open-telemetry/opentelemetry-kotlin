package io.opentelemetry.kotlin.integration.test.logging

import io.opentelemetry.kotlin.integration.test.IntegrationTestHarness
import io.opentelemetry.kotlin.logging.LoggerConfigImpl
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
}
