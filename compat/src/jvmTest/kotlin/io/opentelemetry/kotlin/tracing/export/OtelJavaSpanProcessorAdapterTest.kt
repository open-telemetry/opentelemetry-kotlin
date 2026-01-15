package io.opentelemetry.kotlin.tracing.export

import fakeInProgressOtelJavaSpanData
import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaContext
import io.opentelemetry.kotlin.aliases.OtelJavaSpan
import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.context.toOtelJavaContext
import io.opentelemetry.kotlin.context.toOtelKotlinContext
import io.opentelemetry.kotlin.framework.OtelKotlinHarness
import io.opentelemetry.kotlin.tracing.ext.storeInContext
import io.opentelemetry.kotlin.tracing.ext.toOtelKotlinSpanContext
import io.opentelemetry.kotlin.tracing.model.ReadWriteSpan
import io.opentelemetry.kotlin.tracing.model.ReadableSpan
import io.opentelemetry.kotlin.tracing.model.Span
import io.opentelemetry.kotlin.tracing.model.SpanContext
import io.opentelemetry.kotlin.tracing.model.SpanKind
import org.junit.Test
import kotlin.test.BeforeTest
import kotlin.test.assertEquals

@OptIn(ExperimentalApi::class)
internal class OtelJavaSpanProcessorAdapterTest {
    private val rootContext = OtelJavaContext.root().toOtelKotlinContext()
    private lateinit var harness: OtelKotlinHarness

    @BeforeTest
    fun setUp() {
        harness = OtelKotlinHarness()
    }

    @Test
    fun `span propagated correctly`() {
        with(harness) {
            val fakeTime = 5_000_000L
            val span = tracer.createSpan(
                name = "test",
                spanKind = SpanKind.CLIENT,
                startTimestamp = fakeTime,
            ) {
                setStringAttribute("key", "value")
                addEvent("event", fakeTime) {
                    setStringAttribute("eventAttr", "value")
                }
                addLink(fakeInProgressOtelJavaSpanData.spanContext.toOtelKotlinSpanContext()) {
                    setStringAttribute("linkAttr", "value")
                }
            }
            config.spanProcessors.add(
                FakeSpanProcessor(
                    startAction = assertInputForSpan(
                        expectedName = "test",
                        expectedParentSpanContextSupplier = { OtelJavaSpan.getInvalid().spanContext.toOtelKotlinSpanContext() },
                    ),
                    endAction = assertReadableSpan(
                        expectedName = "test",
                        expectedSpanSupplier = { span }
                    ),
                    endingAction = assertReadableSpan(
                        expectedName = "test",
                        expectedSpanSupplier = { span }
                    )
                )
            )
            span.end()
        }
    }

    @Test
    fun `parent context propagated correctly`() {
        with(harness) {
            val parentSpan = tracer.createSpan("parent")
            val childSpan = tracer.createSpan(
                name = "name",
                parentContext = parentSpan.storeInContext(rootContext)
            )

            config.spanProcessors.add(
                FakeSpanProcessor(
                    startAction = assertInputForSpan(
                        expectedName = "name",
                        expectedParentSpanContextSupplier = { parentSpan.spanContext },
                    ),
                    endAction = assertReadableSpan(
                        expectedName = "name",
                        expectedSpanSupplier = { childSpan }
                    )
                )
            )
            childSpan.end()
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
        expectedSpanSupplier: () -> Span
    ): (span: ReadableSpan) -> Unit {
        return fun(span: ReadableSpan) {
            if (span.name == expectedName) {
                with(expectedSpanSupplier()) {
                    assertEquals(spanContext, span.spanContext)
                    assertEquals(parent, span.parent)
                    assertEquals(spanKind, span.spanKind)
                    assertEquals(startTimestamp, span.startTimestamp)
                    assertEquals(name, span.name)
                    assertEquals(status, span.status)
                    assertEquals(isRecording(), span.hasEnded)
                    assertEquals(attributes.toMap(), span.attributes)
                    assertEquals(events, span.events)
                    assertEquals(links, span.links)
                }
            }
        }
    }
}
