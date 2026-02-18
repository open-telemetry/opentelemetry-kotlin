package io.opentelemetry.kotlin.tracing

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.assertions.assertSpanContextsMatch
import io.opentelemetry.kotlin.attributes.MutableAttributeContainer
import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.export.OperationResultCode
import io.opentelemetry.kotlin.framework.OtelKotlinHarness
import io.opentelemetry.kotlin.tracing.data.StatusData
import io.opentelemetry.kotlin.tracing.export.SpanProcessor
import io.opentelemetry.kotlin.tracing.ext.storeInContext
import io.opentelemetry.kotlin.tracing.ext.toOtelJavaTraceFlags
import io.opentelemetry.kotlin.tracing.model.ReadWriteSpan
import io.opentelemetry.kotlin.tracing.model.ReadableSpan
import io.opentelemetry.kotlin.tracing.model.SpanContext
import io.opentelemetry.kotlin.tracing.model.SpanKind
import io.opentelemetry.kotlin.tracing.model.SpanRelationships
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

@OptIn(ExperimentalApi::class)
internal class SpanExportTest {

    private lateinit var harness: OtelKotlinHarness

    @BeforeTest
    fun setUp() = runTest {
        harness = OtelKotlinHarness(testScheduler)
        harness.config.spanLimits = {
            attributeCountLimit = 100
            linkCountLimit = 100
            eventCountLimit = 100
            attributeCountPerLinkLimit = 100
            attributeCountPerEventLimit = 100
        }
    }

    @Test
    fun `test minimal span export`() = runTest {
        val spanName = "my_span"
        harness.tracer.startSpan(spanName).end()

        harness.assertSpans(
            expectedCount = 1,
            goldenFileName = "span_minimal.json",
        )
    }

    @Test
    fun `test span properties export`() = runTest {
        val spanName = "my_span"
        val span = harness.tracer.startSpan(
            name = spanName,
            spanKind = SpanKind.CLIENT,
            startTimestamp = 500
        )
        assertEquals(spanName, span.name)

        val name = "new_name"
        span.name = name
        assertEquals(name, span.name)

        assertEquals(StatusData.Unset, span.status)
        span.status = StatusData.Ok
        assertEquals(StatusData.Ok, span.status)

        assertTrue(span.isRecording())
        span.end(1000)
        assertFalse(span.isRecording())

        assertEquals(SpanKind.CLIENT, span.spanKind)
        assertEquals(500, span.startTimestamp)
        harness.assertSpans(
            expectedCount = 1,
            goldenFileName = "span_props.json",
        )
    }

    @Test
    fun `test span attributes export`() = runTest {
        val spanName = "span_attrs"
        val span = harness.tracer.startSpan(spanName)
        span.assertAttributes()
        span.end()

        harness.assertSpans(
            expectedCount = 1,
            goldenFileName = "span_attrs.json",
        )
    }

    @Test
    fun `test span events export`() = runTest {
        val spanName = "span_events"
        val span = harness.tracer.startSpan(spanName).apply {
            assertTrue(events.isEmpty())

            val eventName = "my_event"
            val eventTimestamp = 150L
            addEvent(eventName, eventTimestamp) {
                assertAttributes()
            }

            val event = events.single()
            assertEquals(eventName, event.name)
            assertEquals(eventTimestamp, event.timestamp)
        }
        span.end()

        harness.assertSpans(
            expectedCount = 1,
            goldenFileName = "span_events.json",
        )
    }

    @Test
    fun `test span context parent`() = runTest {
        val root = harness.kotlinApi.contextFactory.root()

        val a = harness.tracer.startSpan("a", parentContext = root)
        val ctxa = a.storeInContext(root)

        val b = harness.tracer.startSpan("b", parentContext = ctxa)
        val ctxb = b.storeInContext(ctxa)

        val c = harness.tracer.startSpan("c", parentContext = ctxb)

        assertSpanContextsMatch(harness.kotlinApi.spanContextFactory.invalid, a.parent)
        assertNotNull(a.spanContext)
        assertSpanContextsMatch(a.spanContext, b.parent)
        assertSpanContextsMatch(b.spanContext, c.parent)
        assertNotNull(c.spanContext)

        a.end()
        b.end()
        c.end()

        harness.assertSpans(3, null) { spans ->
            val exportA = spans[0]
            val exportB = spans[1]
            val exportC = spans[2]

            assertFalse(exportA.parent.isValid)
            assertNotNull(exportA.spanContext)
            assertSpanContextsMatch(exportA.spanContext, exportB.parent)
            assertSpanContextsMatch(exportB.spanContext, exportC.parent)
            assertNotNull(exportC.spanContext)
        }
    }

    @Test
    fun `test span trace flags`() = runTest {
        val span = harness.tracer.startSpan("my_span")
        val flags = span.spanContext.traceFlags
        assertEquals("01", flags.toOtelJavaTraceFlags().asHex())
        assertTrue(flags.isSampled)
        assertFalse(flags.isRandom)
    }

    @Test
    fun `test invalid span context`() {
        val invalidContext = harness.kotlinApi.spanContextFactory.invalid

        // Test invalid context properties
        assertFalse(invalidContext.isValid)
        assertEquals("00000000000000000000000000000000", invalidContext.traceId)
        assertEquals("0000000000000000", invalidContext.spanId)

        // Test span creation with invalid parent
        val span = harness.tracer.startSpan(
            "test_span",
            parentContext = harness.kotlinApi.contextFactory.root()
        )

        // Child span should be created with a valid context
        assertTrue(span.spanContext.isValid)
        assertNotEquals(invalidContext.traceId, span.spanContext.traceId)
        assertNotEquals(invalidContext.spanId, span.spanContext.spanId)

        span.end()
    }

    @Test
    fun `test span links export`() = runTest {
        val linkedSpan = harness.tracer.startSpan("linked_span")
        val span = harness.tracer.startSpan("span_links").apply {
            assertTrue(events.isEmpty())

            addLink(linkedSpan.spanContext) {
                assertAttributes()
            }

            val link = links.single()
            assertEquals(linkedSpan.spanContext, link.spanContext)
        }
        span.end()
        linkedSpan.end()

        harness.assertSpans(
            expectedCount = 2,
            goldenFileName = "span_links.json",
        )
    }

    @Test
    fun `test tracer with schema url and attributes`() = runTest {
        val schemaUrl = "https://opentelemetry.io/schemas/1.21.0"
        val tracerWithSchemaUrl = harness.tracerProvider.getTracer(
            name = "test-tracer",
            version = "2.0.0",
            schemaUrl = schemaUrl
        ) {
            setStringAttribute("tracer_attr", "tracer_value")
            setLongAttribute("tracer_id", 123)
        }

        val span = tracerWithSchemaUrl.startSpan("schema_url_span")
        span.end()

        harness.assertSpans(expectedCount = 1, goldenFileName = "span_schema_url.json")
    }

    @Test
    fun `test multiple operations`() = runTest {
        // create multiple spans, with multiple links and events
        val linkedSpan1 = harness.tracer.startSpan("linked_span_1")
        val linkedSpan2 = harness.tracer.startSpan("linked_span_2")
        val linkedSpan3 = harness.tracer.startSpan("linked_span_3")

        val span = harness.tracer.startSpan("multi_operations_span").apply {
            // Add multiple events
            addEvent("event_1", 100L)
            addEvent("event_2", 200L) {
                setStringAttribute("event_attr", "value")
            }
            addEvent("event_3", 300L)

            // Add multiple links
            addLink(linkedSpan1.spanContext)
            addLink(linkedSpan2.spanContext) {
                setStringAttribute("link_attr", "link_value")
            }
            addLink(linkedSpan3.spanContext)

            // Verify counts
            assertEquals(3, events.size)
            assertEquals(3, links.size)
        }

        span.end()
        linkedSpan1.end()
        linkedSpan2.end()
        linkedSpan3.end()

        harness.assertSpans(expectedCount = 4, goldenFileName = "span_multiple_operations.json")
    }

    @Test
    fun `test attributes edge cases`() = runTest {
        val span = harness.tracer.startSpan("edge_case_attributes").apply {
            // Test empty string
            setStringAttribute("empty_string", "")

            // Test empty lists
            setStringListAttribute("empty_string_list", emptyList())
            setBooleanListAttribute("empty_bool_list", emptyList())
            setLongListAttribute("empty_long_list", emptyList())
            setDoubleListAttribute("empty_double_list", emptyList())

            // Test whitespace
            setStringAttribute("whitespace_only", " ")

            // Test lists with empty elements
            setStringListAttribute("list_with_empty", listOf("", "non-empty", "", "another-value"))
        }

        span.end()

        harness.assertSpans(expectedCount = 1, goldenFileName = "span_edge_case_attributes.json")
    }

    @Test
    fun `test trace and span id validation without sanitization`() = runTest {
        val span1 = harness.tracer.startSpan("validation_span_1")
        val span2 = harness.tracer.startSpan("validation_span_2")
        val ctx = span1.storeInContext(harness.kotlinApi.contextFactory.root())
        val span3 = harness.tracer.startSpan("validation_span_3", ctx)

        span1.end()
        span2.end()
        span3.end()

        harness.assertSpans(3, null) { spans ->
            val validationSpan1 = spans.first { it.name == "validation_span_1" }
            val validationSpan2 = spans.first { it.name == "validation_span_2" }
            val validationSpan3 = spans.first { it.name == "validation_span_3" }

            // Validate ID formats and hex characters
            listOf(validationSpan1, validationSpan2, validationSpan3).forEach {
                it.spanContext.assertValidIds()
            }

            // Trace IDs should be the same for a parent-child set of spans. Otherwise, they should be different.
            assertEquals(validationSpan1.spanContext.traceId, validationSpan3.spanContext.traceId)
            assertNotEquals(
                validationSpan1.spanContext.traceId,
                validationSpan2.spanContext.traceId
            )

            // All span IDs should be unique
            val spanIds =
                setOf(
                    validationSpan1.spanContext.spanId,
                    validationSpan2.spanContext.spanId,
                    validationSpan3.spanContext.spanId
                )
            assertEquals(3, spanIds.size)

            // Root spans have invalid parents
            listOf(validationSpan1, validationSpan2).forEach {
                assertEquals("00000000000000000000000000000000", it.parent.traceId)
            }
        }
    }

    // IDs should be valid hex strings with correct OpenTelemetry lengths (trace: 32 chars, span: 16 chars)
    private fun SpanContext.assertValidIds() {
        assertEquals(32, traceId.length)
        assertEquals(16, spanId.length)
        val hexPattern = Regex("[0-9a-f]+")
        assertTrue(traceId.matches(hexPattern))
        assertTrue(spanId.matches(hexPattern))
    }

    private fun MutableAttributeContainer.assertAttributes() {
        assertTrue(attributes.isEmpty())

        // set attributes
        setStringAttribute("string_key", "value")
        setStringAttribute("string_key", "second_value")
        setBooleanAttribute("bool_key", true)
        setLongAttribute("long_key", 42)
        setDoubleAttribute("double_key", 3.14)
        setStringListAttribute("string_list_key", listOf("a"))
        setBooleanListAttribute("bool_list_key", listOf(true))
        setLongListAttribute("long_list_key", listOf(42))
        setDoubleListAttribute("double_list_key", listOf(3.14))

        val observed = attributes
        val expected = mapOf(
            "string_key" to "second_value",
            "bool_key" to true,
            "long_key" to 42L,
            "double_key" to 3.14,
            "string_list_key" to listOf("a"),
            "bool_list_key" to listOf(true),
            "long_list_key" to listOf(42L),
            "double_list_key" to listOf(3.14),
        )
        assertEquals(expected.size, observed.size)
        expected.forEach { entry ->
            assertEquals(entry.value, observed[entry.key])
        }
    }

    @Test
    fun `test tracer provider resource export`() = runTest {
        harness.config.apply {
            schemaUrl = "https://example.com/some_schema.json"
            attributes = {
                setStringAttribute("service.name", "test-service")
                setStringAttribute("service.version", "1.0.0")
                setStringAttribute("environment", "test")
            }
        }

        val tracer = harness.kotlinApi.tracerProvider.getTracer("test_tracer")
        tracer.startSpan("test_span").end()

        harness.assertSpans(
            expectedCount = 1,
            goldenFileName = "span_resource.json",
        )
    }

    @Test
    fun `test context is passed to processor`() {
        // Create a SpanProcessor that captures any passed Context.
        val contextCapturingProcessor = ContextCapturingProcessor()
        harness.config.spanProcessors.add(contextCapturingProcessor)

        // Create a context key and add a test value
        val currentContext = harness.kotlinApi.contextFactory.implicitContext()
        val contextKey = currentContext.createKey<String>("best_team")
        val testContextValue = "independiente"
        val testContext = currentContext.set(contextKey, testContextValue)

        // Create a span with the created context
        val tracer = harness.kotlinApi.tracerProvider.getTracer("test_tracer")
        tracer.startSpan("Test span with context", parentContext = testContext).end()

        // Verify context was captured and contains expected value
        val actualValue = contextCapturingProcessor.capturedContext?.get(contextKey)
        assertSame(testContextValue, actualValue)
    }

    @Test
    fun `test span limit export`() = runTest {
        harness.config.spanLimits = {
            attributeCountLimit = 1
            linkCountLimit = 1
            eventCountLimit = 1
            attributeCountPerLinkLimit = 1
            attributeCountPerEventLimit
        }
        val a = harness.tracer.startSpan("a")
        val b = harness.tracer.startSpan("b")
        val c = harness.tracer.startSpan("span", null, SpanKind.INTERNAL, null, {
            addMultipleAttrs()

            addLink(a.spanContext) {
                addMultipleAttrs()
            }
            addLink(b.spanContext) {
                addMultipleAttrs()
            }
            addEvent("first") {
                addMultipleAttrs()
            }
            addEvent("second") {
                addMultipleAttrs()
            }
        })
        a.end()
        b.end()
        c.end()
        harness.assertSpans(3, "span_limits.json")
    }

    @Test
    fun `test log export with custom processor`() = runTest {
        var link: SpanContext? = null
        harness.config.spanProcessors.add(
            CustomSpanProcessor {
                link
            }
        )
        val other = harness.tracer.startSpan("other")
        link = other.spanContext
        other.end()
        harness.tracer.startSpan("my_span").end()

        harness.assertSpans(
            expectedCount = 2,
            goldenFileName = "span_custom_processor.json",
        )
    }

    private fun SpanRelationships.addMultipleAttrs() {
        setStringAttribute("key1", "value")
        setStringAttribute("key2", "value")
    }

    /**
     * Custom processor that captures the context passed to onStart
     */
    private class ContextCapturingProcessor : SpanProcessor {
        var capturedContext: Context? = null

        override fun onStart(span: ReadWriteSpan, parentContext: Context) {
            capturedContext = parentContext
        }

        override fun onEnding(span: ReadWriteSpan) {
        }

        override fun onEnd(span: ReadableSpan) {}
        override fun isStartRequired(): Boolean = true
        override fun isEndRequired(): Boolean = false
        override suspend fun shutdown(): OperationResultCode = OperationResultCode.Success
        override suspend fun forceFlush(): OperationResultCode = OperationResultCode.Success
    }

    /**
     * Custom processor that alters spans
     */
    private class CustomSpanProcessor(private val link: () -> SpanContext?) : SpanProcessor {

        override fun onStart(
            span: ReadWriteSpan,
            parentContext: Context
        ) {
            with(span) {
                name = "override"
                status = StatusData.Error("bad_err")

                setStringAttribute("string", "value")
                setBooleanAttribute("bool", false)
                setDoubleAttribute("double", 5.4)
                setLongAttribute("long", 5L)
                setStringListAttribute("stringList", listOf("value"))
                setBooleanListAttribute("boolList", listOf(false))
                setDoubleListAttribute("doubleList", listOf(5.4))
                setLongListAttribute("longList", listOf(5L))

                link()?.let {
                    addLink(it) {
                        setStringAttribute("key", "value")
                    }
                }

                addEvent("custom_event", 30) {
                    setStringAttribute("key", "value")
                }
            }
        }

        override fun onEnding(span: ReadWriteSpan) {
        }

        override fun onEnd(span: ReadableSpan) {
        }

        override suspend fun shutdown(): OperationResultCode = OperationResultCode.Success
        override suspend fun forceFlush(): OperationResultCode = OperationResultCode.Success
        override fun isStartRequired(): Boolean = true
        override fun isEndRequired(): Boolean = true
    }
}
