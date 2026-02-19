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
internal class SpanProcessNaughtyOnEndTest {

    private lateinit var harness: IntegrationTestHarness

    @BeforeTest
    fun setUp() = runTest {
        harness =
            IntegrationTestHarness(testScheduler)
    }

    @Test
    fun testSpanProcessorOnEndOverrideIgnored() = runTest {
        harness.config.spanProcessors.add(NaughtySpanProcessor())
        harness.tracer.startSpan("span") {
            setStringAttribute("key", "value")
            addEvent("test")
            addLink(FakeSpanContext.INVALID)
        }.end(500)
        harness.assertSpans(
            expectedCount = 1,
            goldenFileName = "span_attempted_override.json",
        )
    }

    private class NaughtySpanProcessor : SpanProcessor {
        private lateinit var ref: ReadWriteSpan

        override fun onStart(
            span: ReadWriteSpan,
            parentContext: Context
        ) {
            ref = span
        }

        override fun onEnding(span: ReadWriteSpan) {
        }

        override fun onEnd(span: ReadableSpan) {
            ref.handleSpan()
        }

        private fun ReadWriteSpan.handleSpan() {
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
            assertEquals(1, events.size)
            assertEquals(1, links.size)

            // assert subset of properties cannot be written
            name = "override"
            status = StatusData.Error("override")
            setStringAttribute("foo", "bar")
            addEvent("test", 5) {
                setStringAttribute("foo", "bar")
            }
            addLink(FakeSpanContext.INVALID) {
                setStringAttribute("foo", "bar")
            }
            end(678)
        }

        override fun isStartRequired(): Boolean = true
        override fun isEndRequired(): Boolean = true
        override suspend fun forceFlush(): OperationResultCode = OperationResultCode.Success
        override suspend fun shutdown(): OperationResultCode = OperationResultCode.Success
    }
}
