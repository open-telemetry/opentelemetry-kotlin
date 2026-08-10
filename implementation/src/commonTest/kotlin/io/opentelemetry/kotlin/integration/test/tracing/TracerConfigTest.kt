package io.opentelemetry.kotlin.integration.test.tracing

import io.opentelemetry.kotlin.integration.test.IntegrationTestHarness
import io.opentelemetry.kotlin.tracing.TracerConfigImpl
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class TracerConfigTest {

    private lateinit var harness: IntegrationTestHarness

    @BeforeTest
    fun setUp() = runTest {
        harness = IntegrationTestHarness(testScheduler)
    }

    @Test
    fun testTracerConfiguratorDisablesMatchingScope() = runTest {
        harness.config.tracerProvider = {
            tracerConfigurator { scope ->
                when (scope.name) {
                    "disabled" -> TracerConfigImpl(false)
                    else -> TracerConfigImpl(true)
                }
            }
        }

        val tracerProvider = harness.tracerProvider
        val disabled = tracerProvider.getTracer("disabled")
        val enabled = tracerProvider.getTracer("enabled")

        assertFalse(disabled.enabled())
        assertTrue(enabled.enabled())

        disabled.startSpan("dropped").end()
        enabled.startSpan("kept").end()

        harness.assertSpans(expectedCount = 1) { spans ->
            assertEquals("kept", spans.single().name)
        }
    }
}
