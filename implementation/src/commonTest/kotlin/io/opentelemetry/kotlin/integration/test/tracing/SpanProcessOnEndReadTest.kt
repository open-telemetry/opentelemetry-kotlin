package io.opentelemetry.kotlin.integration.test.tracing

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.export.OperationResultCode
import io.opentelemetry.kotlin.integration.test.IntegrationTestHarness
import io.opentelemetry.kotlin.tracing.FakeSpanContext
import io.opentelemetry.kotlin.tracing.data.StatusData
import io.opentelemetry.kotlin.tracing.export.SpanProcessor
import io.opentelemetry.kotlin.tracing.model.ReadWriteSpan
import io.opentelemetry.kotlin.tracing.model.ReadableSpan
import io.opentelemetry.kotlin.tracing.model.SpanKind
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalApi::class)
internal class SpanProcessOnEndReadTest {

    private lateinit var harness: IntegrationTestHarness

    @BeforeTest
    fun setUp() = runTest {
        harness =
            IntegrationTestHarness(testScheduler)
    }

    @Test
    fun testReadPropertiesInProcessor() = runTest {
        harness.config.spanProcessors.add(OnEndSpanProcessor())
        harness.tracer.startSpan("span", null, SpanKind.INTERNAL, null) {
            setStringAttribute("key", "value")
            addEvent("test")
            addLink(FakeSpanContext.INVALID) {
                setStringAttribute("foo", "bar")
            }
        }.end()
        harness.assertSpans(
            expectedCount = 1,
            goldenFileName = "span_read_on_end.json",
        )
    }

    private class OnEndSpanProcessor : SpanProcessor {

        private fun ReadableSpan.handleSpan() {
            // assert properties can be read
            assertEquals("span", name)
            assertEquals(StatusData.Unset, status)
            assertFalse(hasEnded)
            assertEquals(SpanKind.INTERNAL, spanKind)
            assertEquals(0, startTimestamp)
            assertNull(endTimestamp)
            assertTrue(spanContext.isValid)
            assertFalse(parent.isValid)
            assertTrue(resource.attributes.isEmpty())
            assertEquals("test_tracer", instrumentationScopeInfo.name)
            assertEquals(mapOf("key" to "value"), attributes)
            assertEquals("test", events.single().name)
            assertEquals("bar", links.single().attributes["foo"])
        }

        override fun onStart(
            span: ReadWriteSpan,
            parentContext: Context
        ) {
        }

        override fun onEnd(span: ReadableSpan) {
            span.handleSpan()
        }

        override fun onEnding(span: ReadWriteSpan) {
        }

        override fun isStartRequired(): Boolean = true
        override fun isEndRequired(): Boolean = true
        override suspend fun forceFlush(): OperationResultCode = OperationResultCode.Success
        override suspend fun shutdown(): OperationResultCode = OperationResultCode.Success
    }
}
