package io.opentelemetry.kotlin.tracing.export

import fakeInProgressOtelJavaSpanData
import io.opentelemetry.kotlin.aliases.OtelJavaContext
import io.opentelemetry.kotlin.aliases.OtelJavaSpan
import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.context.toOtelJavaContext
import io.opentelemetry.kotlin.context.toOtelKotlinContext
import io.opentelemetry.kotlin.framework.OtelKotlinHarness
import io.opentelemetry.kotlin.tracing.Span
import io.opentelemetry.kotlin.tracing.SpanContext
import io.opentelemetry.kotlin.tracing.SpanKind
import io.opentelemetry.kotlin.tracing.ext.storeInContext
import io.opentelemetry.kotlin.tracing.ext.toOtelKotlinSpanContext
import io.opentelemetry.kotlin.tracing.model.ReadWriteSpan
import io.opentelemetry.kotlin.tracing.model.ReadableSpan
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.BeforeTest
import kotlin.test.assertEquals

internal class OtelJavaSpanProcessorAdapterTest {
    private val rootContext = OtelJavaContext.root().toOtelKotlinContext()
    private lateinit var harness: OtelKotlinHarness

    @BeforeTest
    fun setUp() = runTest {
        harness = OtelKotlinHarness(testScheduler)
    }

    @Test
    fun `span propagated correctly`() {
        with(harness) {
            val fakeTime = 5_000_000L
            var startCalled = false
            var endingCalled = false
            var endCalled = false
            config.spanProcessors.add(
                FakeSpanProcessor(
                    startAction = { span, context ->
                        startCalled = true
                        assertInputForSpan(
                            expectedName = "test",
                            expectedParentSpanContextSupplier = { OtelJavaSpan.getInvalid().spanContext.toOtelKotlinSpanContext() },
                        )(span, context)
                    },
                    endAction = {
                        endCalled = true
                        assertReadableSpan(expectedName = "test")(it)
                    },
                    endingAction = {
                        endingCalled = true
                        assertReadWriteSpan(expectedName = "test")(it)
                    }
                )
            )

            val span = tracer.startSpan(
                name = "test",
                spanKind = SpanKind.CLIENT,
                startTimestamp = fakeTime,
                action = {
                    setStringAttribute("key", "value")
                    addLink(fakeInProgressOtelJavaSpanData.spanContext.toOtelKotlinSpanContext()) {
                        setStringAttribute("linkAttr", "value")
                    }
                }
            ).apply {
                addEvent("event", fakeTime) {
                    setStringAttribute("eventAttr", "value")
                }
            }
            span.end()

            assertEquals(true, startCalled)
            assertEquals(true, endingCalled)
            assertEquals(true, endCalled)
        }
    }

    @Test
    fun `onEnding invoked when required`() {
        with(harness) {
            val processor = FakeSpanProcessor(onEndingRequired = true)
            config.spanProcessors.add(processor)

            tracer.startSpan("test").end()

            assertEquals(1, processor.endingCalls.size)
            assertEquals("test", processor.endingCalls.single().name)
        }
    }

    @Test
    fun `isOnEndingRequired delegates to impl`() {
        assertEquals(
            true,
            OtelJavaSpanProcessorAdapter(FakeSpanProcessor(onEndingRequired = true)).isOnEndingRequired()
        )
        assertEquals(
            false,
            OtelJavaSpanProcessorAdapter(FakeSpanProcessor(onEndingRequired = false)).isOnEndingRequired()
        )
    }

    @Test
    fun `parent context propagated correctly`() {
        with(harness) {
            lateinit var parentSpan: Span
            var startCalled = false

            config.spanProcessors.add(
                FakeSpanProcessor(
                    startAction = { span, context ->
                        startCalled = true
                        assertInputForSpan(
                            expectedName = "name",
                            expectedParentSpanContextSupplier = { parentSpan.spanContext },
                        )(span, context)
                    },
                    endAction = assertReadableSpan(expectedName = "name")
                )
            )

            parentSpan = tracer.startSpan("parent")
            val childSpan = tracer.startSpan(
                name = "name",
                parentContext = parentSpan.storeInContext(rootContext)
            )
            childSpan.end()

            assertEquals(true, startCalled)
        }
    }

    private fun assertInputForSpan(
        expectedName: String,
        expectedParentSpanContextSupplier: () -> SpanContext? = { null },
    ): (span: ReadWriteSpan, context: Context) -> Unit {
        return fun(span: ReadWriteSpan, parentContext: Context) {
            if (expectedName == span.name) {
                val parentSpanContext = expectedParentSpanContextSupplier()
                if (parentSpanContext != null) {
                    with(parentSpanContext) {
                        val spanContextFromContext = OtelJavaSpan.fromContext(parentContext.toOtelJavaContext()).spanContext
                        assertEquals(spanId, spanContextFromContext.spanId)
                        if (parentSpanContext.isValid) {
                            assertEquals(traceId, span.spanContext.traceId)
                        }
                    }
                }
            }
        }
    }

    private fun assertReadableSpan(
        expectedName: String,
    ): (span: ReadableSpan) -> Unit {
        return fun(span: ReadableSpan) {
            if (span.name == expectedName) {
                assertEquals(expectedName, span.name)
            }
        }
    }

    private fun assertReadWriteSpan(
        expectedName: String,
    ): (span: ReadWriteSpan) -> Unit {
        return fun(span: ReadWriteSpan) {
            if (span.name == expectedName) {
                assertEquals(expectedName, span.name)
                assertEquals("value", span.attributes["key"])
            }
        }
    }
}
