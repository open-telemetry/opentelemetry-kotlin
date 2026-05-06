package io.opentelemetry.kotlin.logging

import io.opentelemetry.kotlin.InstrumentationScopeInfoImpl
import io.opentelemetry.kotlin.attributes.AttributesMutator
import io.opentelemetry.kotlin.clock.FakeClock
import io.opentelemetry.kotlin.export.MutableShutdownState
import io.opentelemetry.kotlin.factory.FakeContextFactory
import io.opentelemetry.kotlin.factory.FakeSpanContextFactory
import io.opentelemetry.kotlin.init.config.LogLimitConfig
import io.opentelemetry.kotlin.logging.export.FakeLogRecordProcessor
import io.opentelemetry.kotlin.resource.FakeResource
import io.opentelemetry.kotlin.tracing.fakeLogLimitsConfig
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class LogAttributesTest {

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
    private lateinit var logger: LoggerImpl
    private val processor = FakeLogRecordProcessor()

    @BeforeTest
    fun setUp() {
        logger = LoggerImpl(
            clock = FakeClock(),
            processor = processor,
            contextFactory = FakeContextFactory(),
            spanContextFactory = FakeSpanContextFactory(),
            key = key,
            resource = FakeResource(),
            logLimitConfig = LogLimitConfig(
                attributeCountLimit = 8,
                attributeValueLengthLimit = fakeLogLimitsConfig.attributeValueLengthLimit
            ),
            shutdownState = MutableShutdownState(),
        )
    }

    @Test
    fun testDefaultLogAttributes() {
        logger.emit("test")
        val log = processor.logs.single()
        assertTrue(log.attributes.isEmpty())

        // ensure returned values is immutable, and not the underlying implementation
        assertTrue(log.attributes !is MutableMap<*, *>)
    }

    @Test
    fun testAddAttributesDuringLogCreation() {
        logger.emit("test") { addTestAttributes() }
        val log = processor.logs.single()
        assertEquals(expected, log.attributes)
    }

    @Test
    fun testAddAttributesAfterLogCreation() {
        logger.emit("test")
        val log = processor.logs.single()
        log.addTestAttributes()
        assertEquals(expected, log.attributes)
    }

    @Test
    fun testAttributesLimitNotExceeded() {
        logger.emit("test") {
            addTestAttributesAlternateValues()
            addTestAttributes()
            addTestAttributes("xyz")
        }
        val log = processor.logs.single()
        assertEquals(expected, log.attributes)
    }

    @Test
    fun testAttributesLimitNotExceeded2() {
        logger.emit("test")
        val log = processor.logs.single()
        log.addTestAttributesAlternateValues()
        log.addTestAttributes()
        log.addTestAttributes("xyz")
        assertEquals(expected, log.attributes)
    }

    @Test
    fun testStringAttrTruncated() {
        val logger = loggerWithValueLengthLimit(3)
        logger.emit("test") { setStringAttribute("key", "abcdef") }
        val log = processor.logs.last()
        assertEquals("abc", log.attributes["key"])
    }

    @Test
    fun testStringListAttrTruncated() {
        val logger = loggerWithValueLengthLimit(2)
        logger.emit("test") { setStringListAttribute("key", listOf("hello", "world")) }
        val log = processor.logs.last()
        @Suppress("UNCHECKED_CAST")
        assertEquals(listOf("he", "wo"), log.attributes["key"] as List<String>)
    }

    @Test
    fun testNonStringAttrsUnaffected() {
        val logger = loggerWithValueLengthLimit(1)
        logger.emit("test") {
            setLongAttribute("long", 123456789L)
            setDoubleAttribute("double", 3.14159)
            setBooleanAttribute("bool", true)
        }
        val log = processor.logs.last()
        assertEquals(123456789L, log.attributes["long"])
        assertEquals(3.14159, log.attributes["double"])
        assertEquals(true, log.attributes["bool"])
    }

    private fun loggerWithValueLengthLimit(limit: Int): LoggerImpl = LoggerImpl(
        clock = FakeClock(),
        processor = processor,
        contextFactory = FakeContextFactory(),
        spanContextFactory = FakeSpanContextFactory(),
        key = key,
        resource = FakeResource(),
        logLimitConfig = LogLimitConfig(
            attributeCountLimit = 8,
            attributeValueLengthLimit = limit,
        ),
        shutdownState = MutableShutdownState(),
    )

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
        setStringAttribute("string", "old-value")
        setDoubleAttribute("double", 6.14)
        setBooleanAttribute("boolean", false)
        setLongAttribute("long", 80000000000000)
        setStringListAttribute("string_list", listOf("stringah"))
        setDoubleListAttribute("double_list", listOf(5.14))
        setBooleanListAttribute("boolean_list", listOf(false))
        setLongListAttribute("long_list", listOf(60000000000000))
    }
}
