package io.opentelemetry.kotlin.tracing

import io.opentelemetry.kotlin.framework.OtelKotlinHarness
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class TracerConfigTest {

    private lateinit var harness: OtelKotlinHarness

    @BeforeTest
    fun setUp() = runTest {
        harness = OtelKotlinHarness(testScheduler)
    }

    @Test
    fun `tracerConfigurator disables matching scope`() = runTest {
        harness.config.tracerProvider = {
            tracerConfigurator { scope ->
                object : TracerConfig {
                    override val enabled = scope.name != "disabled"
                }
            }
        }

        val tracerProvider = harness.kotlinApi.tracerProvider
        tracerProvider.getTracer("disabled").startSpan("dropped").end()
        tracerProvider.getTracer("enabled").startSpan("kept").end()

        harness.assertSpans(expectedCount = 1) { spans ->
            assertEquals("kept", spans.single().name)
        }
    }
}
