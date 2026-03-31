package io.opentelemetry.kotlin.tracing

import io.opentelemetry.kotlin.InstrumentationScopeInfoImpl
import io.opentelemetry.kotlin.attributes.AttributesMutator
import io.opentelemetry.kotlin.clock.FakeClock
import io.opentelemetry.kotlin.export.MutableShutdownState
import io.opentelemetry.kotlin.factory.FakeContextFactory
import io.opentelemetry.kotlin.factory.FakeIdGenerator
import io.opentelemetry.kotlin.factory.FakeSpanContextFactory
import io.opentelemetry.kotlin.factory.FakeSpanFactory
import io.opentelemetry.kotlin.factory.FakeTraceFlagsFactory
import io.opentelemetry.kotlin.init.config.SpanLimitConfig
import io.opentelemetry.kotlin.resource.FakeResource
import io.opentelemetry.kotlin.tracing.export.FakeSpanProcessor
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class SpanAttributesTest {

    private val attributeLimit = 8
    private val expected = mapOf(
        "string" to "value",
        "double" to 3.14,
        "boolean" to true,
        "long" to 90000000000000,
        "string_list" to listOf("string"),
        "double_list" to listOf(3.14),
        "boolean_list" to listOf(true),
        "long_list" to listOf(90000000000000)
    )

    private val key = InstrumentationScopeInfoImpl("key", null, null, emptyMap())
    private lateinit var spanLimitConfig: SpanLimitConfig
    private lateinit var tracer: TracerImpl

    @BeforeTest
    fun setUp() {
        spanLimitConfig = SpanLimitConfig(
            attributeCountLimit = attributeLimit,
            attributeValueLengthLimit = Int.MAX_VALUE,
            linkCountLimit = fakeSpanLimitsConfig.linkCountLimit,
            eventCountLimit = fakeSpanLimitsConfig.eventCountLimit,
            attributeCountPerEventLimit = fakeSpanLimitsConfig.attributeCountPerEventLimit,
            attributeCountPerLinkLimit = fakeSpanLimitsConfig.attributeCountPerLinkLimit
        )
        tracer = TracerImpl(
            clock = FakeClock(),
            processor = FakeSpanProcessor(),
            contextFactory = FakeContextFactory(),
            spanContextFactory = FakeSpanContextFactory(),
            traceFlagsFactory = FakeTraceFlagsFactory(),
            spanFactory = FakeSpanFactory(),
            scope = key,
            resource = FakeResource(),
            spanLimitConfig = spanLimitConfig,
            idGenerator = FakeIdGenerator(),
            shutdownState = MutableShutdownState(),
        )
    }

    @Test
    fun testSpanDefaultAttributes() {
        val span = tracer.startSpan("test")
        assertTrue(span.toReadableSpan().attributes.isEmpty())

        // ensure returned values is immutable, and not the underlying implementation
        assertTrue((span.toReadableSpan()).attributes !is MutableMap<*, *>)
    }

    @Test
    fun testSpanAddAttributesDuringCreation() {
        val span = tracer.startSpan("test") { addTestAttributes() }
        assertEquals(expected, (span.toReadableSpan()).attributes)
    }

    @Test
    fun testSpanAddAttributesAfterCreation() {
        val span = tracer.startSpan("test")
        span.addTestAttributes()
        assertEquals(expected, (span.toReadableSpan()).attributes)
    }

    @Test
    fun testSpanAddAttributesAfterEnd() {
        val span = tracer.startSpan("test")
        span.addTestAttributes()
        assertEquals(expected, (span.toReadableSpan()).attributes)
        span.end()
        span.addTestAttributesAlternateValues()
        assertEquals(expected, (span.toReadableSpan()).attributes)
    }

    @Test
    fun testAttributesLimitNotExceeded() {
        val span = tracer.startSpan("test", action = {
            addTestAttributesAlternateValues()
            addTestAttributes("xyz")
            addTestAttributes()
        }).apply {
            end()
        }

        assertEquals(expected, (span.toReadableSpan()).attributes)
    }

    @Test
    fun testAttributesLimitNotExceeded2() {
        val span = tracer.startSpan("test").apply {
            addTestAttributesAlternateValues()
            addTestAttributes("xyz")
            addTestAttributes()
            end()
        }

        assertEquals(expected, (span.toReadableSpan()).attributes)
    }

    @Test
    fun testSpanStringAttrTruncated() {
        val tracer = tracerWithValueLengthLimit(3)
        val span = tracer.startSpan("test")
        span.setStringAttribute("key", "abcdef")
        assertEquals("abc", span.toReadableSpan().attributes["key"])
    }

    @Test
    fun testSpanStringListAttrTruncated() {
        val tracer = tracerWithValueLengthLimit(2)
        val span = tracer.startSpan("test")
        span.setStringListAttribute("key", listOf("hello", "world"))
        @Suppress("UNCHECKED_CAST")
        val result = span.toReadableSpan().attributes["key"] as List<String>
        assertEquals(listOf("he", "wo"), result)
    }

    @Test
    fun testSpanNonStringAttributeNotTruncated() {
        val tracer = tracerWithValueLengthLimit(3)
        val span = tracer.startSpan("test")
        span.setLongAttribute("long", 123456789L)
        span.setDoubleAttribute("double", 3.14159)
        span.setBooleanAttribute("bool", true)
        assertEquals(123456789L, span.toReadableSpan().attributes["long"])
        assertEquals(3.14159, span.toReadableSpan().attributes["double"])
        assertEquals(true, span.toReadableSpan().attributes["bool"])
    }

    @Test
    fun testEventStringAttrTruncated() {
        val tracer = tracerWithValueLengthLimit(3)
        val span = tracer.startSpan("test")
        span.addEvent("evt") {
            setStringAttribute("key", "abcdef")
        }
        val eventAttrs = span.toReadableSpan().events.first().attributes
        assertEquals("abc", eventAttrs["key"])
    }

    @Test
    fun testLinkStringAttrTruncated() {
        val tracer = tracerWithValueLengthLimit(3)
        val span = tracer.startSpan("test")
        span.addLink(FakeSpanContext()) {
            setStringAttribute("key", "abcdef")
        }
        val linkAttrs = span.toReadableSpan().links.first().attributes
        assertEquals("abc", linkAttrs["key"])
    }

    private fun tracerWithValueLengthLimit(limit: Int): TracerImpl {
        val config = SpanLimitConfig(
            attributeCountLimit = attributeLimit,
            attributeValueLengthLimit = limit,
            linkCountLimit = fakeSpanLimitsConfig.linkCountLimit,
            eventCountLimit = fakeSpanLimitsConfig.eventCountLimit,
            attributeCountPerEventLimit = fakeSpanLimitsConfig.attributeCountPerEventLimit,
            attributeCountPerLinkLimit = fakeSpanLimitsConfig.attributeCountPerLinkLimit
        )
        return TracerImpl(
            clock = FakeClock(),
            processor = FakeSpanProcessor(),
            contextFactory = FakeContextFactory(),
            spanContextFactory = FakeSpanContextFactory(),
            traceFlagsFactory = FakeTraceFlagsFactory(),
            spanFactory = FakeSpanFactory(),
            scope = key,
            resource = FakeResource(),
            spanLimitConfig = config,
            idGenerator = FakeIdGenerator(),
            shutdownState = MutableShutdownState(),
        )
    }

    private fun AttributesMutator.addTestAttributes(keyToken: String = "") {
        setStringAttribute("string$keyToken", "value")
        setDoubleAttribute("double$keyToken", 3.14)
        setBooleanAttribute("boolean$keyToken", true)
        setLongAttribute("long$keyToken", 90000000000000)
        setStringListAttribute("string_list$keyToken", listOf("string"))
        setDoubleListAttribute("double_list$keyToken", listOf(3.14))
        setBooleanListAttribute("boolean_list$keyToken", listOf(true))
        setLongListAttribute("long_list$keyToken", listOf(90000000000000))
    }

    private fun AttributesMutator.addTestAttributesAlternateValues() {
        setStringAttribute("string", "override")
        setDoubleAttribute("double", 5.4)
        setBooleanAttribute("boolean", false)
        setLongAttribute("long", 80000000000000)
        setStringListAttribute("string_list", listOf("override"))
        setDoubleListAttribute("double_list", listOf(5.4))
        setBooleanListAttribute("boolean_list", listOf(false))
        setLongListAttribute("long_list", listOf(80000000000000))
    }
}
