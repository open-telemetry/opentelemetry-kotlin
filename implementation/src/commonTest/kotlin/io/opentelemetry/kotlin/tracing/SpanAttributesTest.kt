package io.opentelemetry.kotlin.tracing

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.InstrumentationScopeInfoImpl
import io.opentelemetry.kotlin.attributes.MutableAttributeContainer
import io.opentelemetry.kotlin.clock.FakeClock
import io.opentelemetry.kotlin.factory.FakeSdkFactory
import io.opentelemetry.kotlin.init.config.SpanLimitConfig
import io.opentelemetry.kotlin.resource.FakeResource
import io.opentelemetry.kotlin.tracing.export.FakeSpanProcessor
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalApi::class)
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
            linkCountLimit = fakeSpanLimitsConfig.linkCountLimit,
            eventCountLimit = fakeSpanLimitsConfig.eventCountLimit,
            attributeCountPerEventLimit = fakeSpanLimitsConfig.attributeCountPerEventLimit,
            attributeCountPerLinkLimit = fakeSpanLimitsConfig.attributeCountPerLinkLimit
        )
        tracer = TracerImpl(
            FakeClock(),
            FakeSpanProcessor(),
            FakeSdkFactory(),
            key,
            FakeResource(),
            spanLimitConfig,
        )
    }

    @Test
    fun testSpanDefaultAttributes() {
        val span = tracer.startSpan("test")
        assertTrue(span.attributes.isEmpty())

        // ensure returned values is immutable, and not the underlying implementation
        assertTrue(span.attributes !is MutableMap<*, *>)
    }

    @Test
    fun testSpanAddAttributesDuringCreation() {
        val span = tracer.startSpan("test") { addTestAttributes() }
        assertEquals(expected, span.attributes)
    }

    @Test
    fun testSpanAddAttributesAfterCreation() {
        val span = tracer.startSpan("test")
        span.addTestAttributes()
        assertEquals(expected, span.attributes)
    }

    @Test
    fun testSpanAddAttributesAfterEnd() {
        val span = tracer.startSpan("test")
        span.addTestAttributes()
        assertEquals(expected, span.attributes)
        span.end()
        span.addTestAttributesAlternateValues()
        assertEquals(expected, span.attributes)
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

        assertEquals(expected, span.attributes)
    }

    @Test
    fun testAttributesLimitNotExceeded2() {
        val span = tracer.startSpan("test").apply {
            addTestAttributesAlternateValues()
            addTestAttributes("xyz")
            addTestAttributes()
            end()
        }

        assertEquals(expected, span.attributes)
    }

    private fun MutableAttributeContainer.addTestAttributes(keyToken: String = "") {
        setStringAttribute("string$keyToken", "value")
        setDoubleAttribute("double$keyToken", 3.14)
        setBooleanAttribute("boolean$keyToken", true)
        setLongAttribute("long$keyToken", 90000000000000)
        setStringListAttribute("string_list$keyToken", listOf("string"))
        setDoubleListAttribute("double_list$keyToken", listOf(3.14))
        setBooleanListAttribute("boolean_list$keyToken", listOf(true))
        setLongListAttribute("long_list$keyToken", listOf(90000000000000))
    }

    private fun MutableAttributeContainer.addTestAttributesAlternateValues() {
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
