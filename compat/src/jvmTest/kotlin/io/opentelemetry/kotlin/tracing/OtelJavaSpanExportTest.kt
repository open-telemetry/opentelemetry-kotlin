package io.opentelemetry.kotlin.tracing

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaAttributeKey
import io.opentelemetry.kotlin.aliases.OtelJavaAttributes
import io.opentelemetry.kotlin.aliases.OtelJavaContext
import io.opentelemetry.kotlin.aliases.OtelJavaContextKey
import io.opentelemetry.kotlin.aliases.OtelJavaSpanKind
import io.opentelemetry.kotlin.aliases.OtelJavaStatusCode
import io.opentelemetry.kotlin.aliases.OtelJavaTracer
import io.opentelemetry.kotlin.aliases.OtelJavaTracerProvider
import io.opentelemetry.kotlin.assertions.assertSpanContextsMatch
import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.context.ContextAdapter
import io.opentelemetry.kotlin.export.OperationResultCode
import io.opentelemetry.kotlin.framework.OtelKotlinHarness
import io.opentelemetry.kotlin.tracing.export.SpanProcessor
import io.opentelemetry.kotlin.tracing.model.ReadWriteSpan
import io.opentelemetry.kotlin.tracing.model.ReadableSpan
import java.util.concurrent.TimeUnit
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

@OptIn(ExperimentalApi::class)
internal class OtelJavaSpanExportTest {

    private lateinit var harness: OtelKotlinHarness

    private val tracerProvider: OtelJavaTracerProvider
        get() = harness.javaApi.tracerProvider

    private val tracer: OtelJavaTracer
        get() = tracerProvider.get("test_tracer", "0.1.0")

    @BeforeTest
    fun setUp() {
        harness = OtelKotlinHarness()
    }

    @Test
    fun `test minimal span export`() {
        val spanName = "my_span"
        tracer.spanBuilder(spanName).startSpan().end()

        harness.assertSpans(
            expectedCount = 1,
            goldenFileName = "span_minimal.json",
        )
    }

    @Test
    fun `test span properties export`() {
        val spanName = "my_span"
        val span = tracer.spanBuilder(spanName)
            .setSpanKind(OtelJavaSpanKind.CLIENT)
            .setStartTimestamp(500, TimeUnit.NANOSECONDS)
            .startSpan()

        span.updateName("new_name")
        span.setStatus(OtelJavaStatusCode.OK)

        assertTrue(span.isRecording)
        span.end(1000, TimeUnit.NANOSECONDS)
        assertFalse(span.isRecording)

        harness.assertSpans(
            expectedCount = 1,
            goldenFileName = "span_props.json",
        )
    }

    @Test
    fun `test span attributes export`() {
        val spanName = "span_attrs"
        val span = tracer.spanBuilder(spanName).startSpan()
        span.setAllAttributes(attrs)
        span.end()

        harness.assertSpans(
            expectedCount = 1,
            goldenFileName = "span_attrs.json",
        )
    }

    @Test
    fun `test span events export`() {
        val spanName = "span_events"
        val span = tracer.spanBuilder(spanName).startSpan().apply {
            val eventName = "my_event"
            val eventTimestamp = 150L
            addEvent(eventName, attrs, eventTimestamp, TimeUnit.NANOSECONDS)
        }
        span.end()

        harness.assertSpans(
            expectedCount = 1,
            goldenFileName = "span_events.json",
        )
    }

    @Test
    fun `test span context parent`() {
        val a = tracer.spanBuilder("a").startSpan()
        val current = OtelJavaContext.current()
        val parentA = current.with(a)

        val b = tracer.spanBuilder("b").setParent(parentA).startSpan()
        val parentB = current.with(b)

        val c = tracer.spanBuilder("c").setParent(parentB).startSpan()
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
    fun `test span trace flags`() {
        val span = tracer.spanBuilder("my_span").startSpan()
        val flags = span.spanContext.traceFlags
        assertEquals("01", flags.asHex())
        assertTrue(flags.isSampled)
    }

    @Test
    fun `test span trace state`() {
        val span = tracer.spanBuilder("my_span").startSpan()
        val state = span.spanContext.traceState
        assertEquals(emptyMap(), state.asMap())
    }

    @Test
    fun `test span links export`() {
        val linkedSpan = tracer.spanBuilder("linked_span").startSpan()
        val span = tracer.spanBuilder("span_links").startSpan().apply {
            addLink(linkedSpan.spanContext, attrs)
        }
        span.end()
        linkedSpan.end()

        harness.assertSpans(
            expectedCount = 2,
            goldenFileName = "span_links.json",
        )
    }

    @Test
    fun `test java tracer builder`() {
        val javaTracer = tracerProvider.tracerBuilder("test-tracer")
            .build()

        val span = javaTracer.spanBuilder("tracer_builder_span").startSpan()
        span.end()

        harness.assertSpans(expectedCount = 1, goldenFileName = "span_tracer_builder.json")
    }

    @Test
    fun `test java tracer with schema url and attributes`() {
        val schemaUrl = "https://opentelemetry.io/schemas/1.21.0"
        val javaTracerWithSchemaUrl = tracerProvider.tracerBuilder("test-tracer")
            .setInstrumentationVersion("2.0.0")
            .setSchemaUrl(schemaUrl)
            .build()

        val span = javaTracerWithSchemaUrl.spanBuilder("schema_url_span").startSpan()
        span.end()

        harness.assertSpans(expectedCount = 1, goldenFileName = "span_schema_url.json")
    }

    @Test
    fun `test java multiple operations`() {
        val linkedSpan1 = tracer.spanBuilder("linked_span_1").startSpan()
        val linkedSpan2 = tracer.spanBuilder("linked_span_2").startSpan()
        val linkedSpan3 = tracer.spanBuilder("linked_span_3").startSpan()

        val eventAttrs = OtelJavaAttributes.builder().put("event_attr", "value").build()
        val linkAttrs = OtelJavaAttributes.builder().put("link_attr", "link_value").build()

        val span = tracer.spanBuilder("multi_operations_span").startSpan().apply {
            // Add multiple events
            addEvent("event_1", 100L, TimeUnit.NANOSECONDS)
            addEvent("event_2", eventAttrs, 200L, TimeUnit.NANOSECONDS)
            addEvent("event_3", 300L, TimeUnit.NANOSECONDS)

            // Add multiple links
            addLink(linkedSpan1.spanContext)
            addLink(linkedSpan2.spanContext, linkAttrs)
            addLink(linkedSpan3.spanContext)
        }

        span.end()
        linkedSpan1.end()
        linkedSpan2.end()
        linkedSpan3.end()

        harness.assertSpans(
            expectedCount = 4,
            goldenFileName = "span_multiple_operations.json",
        )
    }

    @Test
    fun `test java attributes edge cases`() {
        val span = tracer.spanBuilder("edge_case_attributes").startSpan().apply {
            // Test empty string
            setAttribute("empty_string", "")

            // Test empty lists
            setAttribute(OtelJavaAttributeKey.stringArrayKey("empty_string_list"), emptyList())
            setAttribute(OtelJavaAttributeKey.booleanArrayKey("empty_bool_list"), emptyList())
            setAttribute(OtelJavaAttributeKey.longArrayKey("empty_long_list"), emptyList())
            setAttribute(OtelJavaAttributeKey.doubleArrayKey("empty_double_list"), emptyList())

            // Test whitespace
            setAttribute("whitespace_only", " ")

            // Test lists with empty elements
            setAttribute(
                OtelJavaAttributeKey.stringArrayKey("list_with_empty"),
                listOf("", "non-empty", "", "another-value")
            )
        }

        span.end()

        harness.assertSpans(
            expectedCount = 1,
            goldenFileName = "span_edge_case_attributes.json",
        )
    }

    @Test
    fun `test java trace and span id validation without sanitization`() {
        val span1 = tracer.spanBuilder("validation_span_1").startSpan()
        val span2 = tracer.spanBuilder("validation_span_2").startSpan()
        val ctx = OtelJavaContext.current().with(span1)
        val span3 = tracer.spanBuilder("validation_span_3").setParent(ctx).startSpan()

        span1.end()
        span2.end()
        span3.end()

        harness.assertSpans(3, null) { spans ->
            val validationSpan1 = spans.first { it.name == "validation_span_1" }
            val validationSpan2 = spans.first { it.name == "validation_span_2" }
            val validationSpan3 = spans.first { it.name == "validation_span_3" }

            // Validate trace IDs are 32 hex characters
            assertTrue(validationSpan1.spanContext.traceId.matches(Regex("^[0-9a-f]{32}$")))
            assertTrue(validationSpan2.spanContext.traceId.matches(Regex("^[0-9a-f]{32}$")))
            assertTrue(validationSpan3.spanContext.traceId.matches(Regex("^[0-9a-f]{32}$")))

            // Validate span IDs are 16 hex characters
            assertTrue(validationSpan1.spanContext.spanId.matches(Regex("^[0-9a-f]{16}$")))
            assertTrue(validationSpan2.spanContext.spanId.matches(Regex("^[0-9a-f]{16}$")))
            assertTrue(validationSpan3.spanContext.spanId.matches(Regex("^[0-9a-f]{16}$")))

            // Validate parent-child relationship
            assertEquals(validationSpan1.spanContext.traceId, validationSpan3.spanContext.traceId)
            assertEquals(
                validationSpan1.spanContext.spanId,
                validationSpan3.parent.spanId
            )
        }
    }

    @Test
    fun `test java tracer provider resource export`() {
        harness.config.apply {
            schemaUrl = "https://example.com/some_schema.json"
            attributes = {
                setStringAttribute("service.name", "test-service")
                setStringAttribute("service.version", "1.0.0")
                setStringAttribute("environment", "test")
            }
        }

        val javaTracer = harness.javaApi.tracerProvider.get("test_tracer")
        javaTracer.spanBuilder("test_span").startSpan().end()

        harness.assertSpans(
            expectedCount = 1,
            goldenFileName = "span_resource.json",
        )
    }

    @Test
    fun `test context is passed to processor`() {
        // Create a processor that can capture the original Java context
        val javaContextCapturingProcessor = JavaContextCapturingProcessor()
        harness.config.spanProcessors.add(javaContextCapturingProcessor)

        // Create a context key and add a test value using Java API
        val javaContextKey = OtelJavaContextKey.named<String>("best_team")
        val testContextValue = "independiente"
        val javaContext = OtelJavaContext.current().with(javaContextKey, testContextValue)

        // Create a span with the created context using Java API
        val javaTracer = harness.javaApi.tracerProvider.get("test_tracer")

        // Make the context current and create span
        javaContext.makeCurrent().use {
            javaTracer.spanBuilder("Test span with context").startSpan().end()
        }

        // Verify context was captured and contains expected value
        val actualValue = javaContextCapturingProcessor.capturedJavaContext?.get(javaContextKey)
        assertSame(testContextValue, actualValue)
    }

    /**
     * Custom processor that captures the original Java context from converted contexts
     */
    private class JavaContextCapturingProcessor : SpanProcessor {
        var capturedJavaContext: OtelJavaContext? = null

        override fun onStart(span: ReadWriteSpan, parentContext: Context) {
            capturedJavaContext = (parentContext as ContextAdapter).impl
        }

        override fun onEnding(span: ReadWriteSpan) {
        }

        override fun onEnd(span: ReadableSpan) = Unit
        override fun isStartRequired(): Boolean = true
        override fun isEndRequired(): Boolean = false
        override fun shutdown(): OperationResultCode = OperationResultCode.Success
        override fun forceFlush(): OperationResultCode = OperationResultCode.Success
    }

    private val attrs = OtelJavaAttributes.builder()
        .put("string_key", "value")
        .put("string_key", "second_value")
        .put("bool_key", true)
        .put("long_key", 42)
        .put("double_key", 3.14)
        .put(OtelJavaAttributeKey.stringArrayKey("string_list_key"), listOf("a"))
        .put(OtelJavaAttributeKey.booleanArrayKey("bool_list_key"), listOf(true))
        .put(OtelJavaAttributeKey.longArrayKey("long_list_key"), listOf(42))
        .put(OtelJavaAttributeKey.doubleArrayKey("double_list_key"), listOf(3.14))
        .build()
}
