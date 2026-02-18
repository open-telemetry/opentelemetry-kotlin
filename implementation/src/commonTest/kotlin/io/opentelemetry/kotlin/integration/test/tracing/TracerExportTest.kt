package io.opentelemetry.kotlin.integration.test.tracing

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.integration.test.IntegrationTestHarness
import io.opentelemetry.kotlin.tracing.data.StatusData
import io.opentelemetry.kotlin.tracing.model.SpanKind
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalApi::class)
internal class TracerExportTest {

    private val spanAttributeLimit = 5
    private val eventLimit = 3
    private val linkLimit = 2
    private lateinit var harness: IntegrationTestHarness

    @BeforeTest
    fun setUp() = runTest {
        harness = IntegrationTestHarness(testScheduler)
            .apply {
                config.spanLimits = {
                    attributeCountLimit = spanAttributeLimit
                    eventCountLimit = eventLimit
                    attributeCountPerEventLimit = spanAttributeLimit
                    linkCountLimit = linkLimit
                    attributeCountPerLinkLimit = spanAttributeLimit
                }
            }
    }

    @Test
    fun testMinimalSpanExport() = runTest {
        val span = harness.tracer.startSpan(
            "test",
            null,
            SpanKind.INTERNAL,
            null
        ) { setStringAttribute("foo", "bar") }
        span.end()
        harness.assertSpans(1, "span_minimal.json")
    }

    @Test
    fun testBasicPropertiesExport() = runTest {
        harness.tracer.startSpan(
            name = "custom_span",
            spanKind = SpanKind.PRODUCER,
            startTimestamp = 500
        ).apply {
            status = StatusData.Error("Whoops")
            end(1000)
        }
        harness.assertSpans(1, "span_basic_props.json")
    }

    @Test
    fun testAttributesExport() = runTest {
        val span = harness.tracer.startSpan("test", null, SpanKind.INTERNAL, null, {
            setStringAttribute("foo", "bar")
            setBooleanAttribute("experiment_enabled", true)
        })
        span.end()
        harness.assertSpans(1, "span_attrs.json")
    }

    @Test
    fun testAttributesAfterCreationExport() = runTest {
        val span = harness.tracer.startSpan("test")
        span.apply {
            setStringAttribute("foo", "bar")
            setBooleanAttribute("experiment_enabled", true)
            end()
        }
        harness.assertSpans(1, "span_attrs.json")
    }

    @Test
    fun testSpanEventExport() = runTest {
        harness.tracer.startSpan("test", null, SpanKind.INTERNAL, null) {
            addEvent("my_event", 500) {
                setStringAttribute("foo", "bar")
            }
        }.end()
        harness.assertSpans(1, "span_event.json")
    }

    @Test
    fun testSpanEventAfterCreationExport() = runTest {
        harness.tracer.startSpan("test").apply {
            addEvent("my_event", 500) {
                setStringAttribute("foo", "bar")
            }
            end()
        }
        harness.assertSpans(1, "span_event.json")
    }

    @Test
    fun testSpanLinkExport() = runTest {
        val linkName = "link"
        val otherName = "other"
        val link = harness.tracer.startSpan(linkName)
        val other = harness.tracer.startSpan(otherName, null, SpanKind.INTERNAL, null) {
            addLink(link.spanContext) {
                setStringAttribute("foo", "bar")
            }
        }
        link.end()
        other.end()
        harness.assertSpans(2, "span_links.json", assertions = {
            val linkSpan = it.single { span -> span.name == linkName }
            val otherSpan = it.single { span -> span.name == otherName }

            assertTrue(linkSpan.links.isEmpty())
            val firstLink = otherSpan.links.single().spanContext
            assertEquals(firstLink.traceId, linkSpan.spanContext.traceId)
            assertEquals(firstLink.spanId, linkSpan.spanContext.spanId)
        })
    }

    @Test
    fun testSpanLinkAfterCreationExport() = runTest {
        val linkName = "link"
        val otherName = "other"
        val link = harness.tracer.startSpan(linkName)
        val other = harness.tracer.startSpan(otherName)
        other.addLink(link.spanContext) {
            setStringAttribute("foo", "bar")
        }
        link.end()
        other.end()
        harness.assertSpans(2, "span_links.json", assertions = {
            val linkSpan = it.single { span -> span.name == linkName }
            val otherSpan = it.single { span -> span.name == otherName }

            assertTrue(linkSpan.links.isEmpty())
            val firstLink = otherSpan.links.single().spanContext
            assertEquals(firstLink.traceId, linkSpan.spanContext.traceId)
            assertEquals(firstLink.spanId, linkSpan.spanContext.spanId)
        })
    }

    @Test
    fun testSpanWithParentExport() = runTest {
        val parentName = "parent"
        val childName = "child"
        val parentSpan = harness.tracer.startSpan(parentName)
        val contextFactory = harness.kotlinApi.contextFactory
        val parentCtx = contextFactory.storeSpan(contextFactory.root(), parentSpan)
        val childSpan = harness.tracer.startSpan(childName, parentContext = parentCtx)
        parentSpan.end()
        childSpan.end()

        harness.assertSpans(2, "span_ancestry.json", assertions = {
            val parent = it.single { span -> span.name == parentName }
            val child = it.single { span -> span.name == childName }

            assertEquals(parent.spanContext.traceId, child.spanContext.traceId)
            assertNotEquals(parent.spanContext.spanId, child.spanContext.spanId)

            assertEquals(parent.spanContext.traceId, child.parent.traceId)
            assertEquals(parent.spanContext.spanId, child.parent.spanId)
        })
    }

    @Test
    fun testSpanLimitExport() = runTest {
        harness.tracer.startSpan("test", null, SpanKind.INTERNAL, null) {
            repeat(spanAttributeLimit + 1) {
                setStringAttribute("key-$it", "value")
            }
            repeat(eventLimit + 1) {
                addEvent("event") {
                    repeat(spanAttributeLimit + 1) {
                        setStringAttribute("key-$it", "value")
                    }
                }
            }
            repeat(linkLimit + 1) {
                val linkedSpan = harness.tracer.startSpan("linkedSpan")
                addLink(linkedSpan.spanContext) {
                    repeat(spanAttributeLimit + 1) {
                        setStringAttribute("key-$it", "value")
                    }
                }
            }
        }.run {
            end()
        }

        harness.assertSpans(expectedCount = 1) { spans ->
            val exportedSpan = spans.single()
            with(exportedSpan) {
                assertEquals(spanAttributeLimit, attributes.size)
                assertEquals(eventLimit, events.size)
                assertEquals(linkLimit, links.size)
                assertEquals(spanAttributeLimit, events.first().attributes.size)
                assertEquals(spanAttributeLimit, links.first().attributes.size)
            }
        }
    }
}
