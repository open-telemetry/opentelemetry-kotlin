package io.opentelemetry.kotlin.logging

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.InstrumentationScopeInfoImpl
import io.opentelemetry.kotlin.attributes.MutableAttributeContainer
import io.opentelemetry.kotlin.clock.FakeClock
import io.opentelemetry.kotlin.factory.FakeSdkFactory
import io.opentelemetry.kotlin.init.config.LogLimitConfig
import io.opentelemetry.kotlin.logging.export.FakeLogRecordProcessor
import io.opentelemetry.kotlin.resource.FakeResource
import io.opentelemetry.kotlin.tracing.fakeLogLimitsConfig
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalApi::class)
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
            FakeClock(),
            processor,
            FakeSdkFactory(),
            key,
            FakeResource(),
            LogLimitConfig(
                attributeCountLimit = 8,
                attributeValueLengthLimit = fakeLogLimitsConfig.attributeValueLengthLimit
            )
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
