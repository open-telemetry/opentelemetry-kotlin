package io.opentelemetry.kotlin.integration.test.tracing

import io.opentelemetry.kotlin.export.TelemetryCloseable
import io.opentelemetry.kotlin.integration.test.IntegrationTestHarness
import io.opentelemetry.kotlin.tracing.FakeSpanContext
import io.opentelemetry.kotlin.tracing.SpanContext
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

    @Test
    fun testDisabledTracerPropagatesParent() = runTest {
        harness.config.tracerProvider = {
            tracerConfigurator { TracerConfigImpl(false) }
        }
        val parent = harness.kotlinApi.context.root().storeSpanContext(FakeSpanContext.VALID)

        val span = harness.tracerProvider.getTracer("disabled").startSpan("span", parentContext = parent)
        assertSameSpanContext(FakeSpanContext.VALID, span.spanContext)
        assertFalse(span.isRecording())
    }

    @Test
    fun testTracerPropagatesParentAfterShutdown() = runTest {
        val parent = harness.kotlinApi.context.root().storeSpanContext(FakeSpanContext.VALID)
        val existing = harness.tracerProvider.getTracer("existing")
        (harness.tracerProvider as TelemetryCloseable).shutdown()

        val created = harness.tracerProvider.getTracer("created")
        listOf(existing, created).forEach { tracer ->
            val span = tracer.startSpan("span", parentContext = parent)
            assertSameSpanContext(FakeSpanContext.VALID, span.spanContext)
            assertFalse(span.isRecording())
        }
    }

    private fun assertSameSpanContext(expected: SpanContext, observed: SpanContext) {
        assertEquals(expected.traceId, observed.traceId)
        assertEquals(expected.spanId, observed.spanId)
    }
}
