package io.opentelemetry.kotlin.logging

import io.opentelemetry.kotlin.framework.OtelKotlinHarness
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

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
        loggerProvider.getLogger("disabled").emit("dropped")
        loggerProvider.getLogger("enabled").emit("kept")

        harness.assertLogRecords(expectedCount = 1) { logs ->
            assertEquals("kept", logs.single().body)
        }
    }
}
