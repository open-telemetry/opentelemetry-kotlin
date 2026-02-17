package io.opentelemetry.kotlin.init

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.attributes.DEFAULT_ATTRIBUTE_LIMIT
import io.opentelemetry.kotlin.clock.FakeClock
import io.opentelemetry.kotlin.logging.export.FakeLogRecordProcessor
import io.opentelemetry.kotlin.logging.export.createCompositeLogRecordProcessor
import io.opentelemetry.kotlin.logging.export.simpleLogRecordProcessor
import io.opentelemetry.kotlin.logging.export.stdoutLogRecordExporter
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalApi::class)
internal class LoggerProviderConfigImplTest {

    private val clock = FakeClock()

    @Test
    fun testDefaultLoggingConfig() {
        val cfg = LoggerProviderConfigImpl(clock).generateLoggingConfig()
        assertTrue(cfg.processors.isEmpty())
        assertTrue(cfg.resource.attributes.isEmpty())
        assertNull(cfg.resource.schemaUrl)

        with(cfg.logLimits) {
            assertEquals(128, attributeCountLimit)
            assertEquals(Int.MAX_VALUE, attributeValueLengthLimit)
        }
    }

    @Test
    fun testOverrideLoggingConfig() {
        val firstProcessor = FakeLogRecordProcessor()
        val secondProcessor = FakeLogRecordProcessor()
        val attrCount = 100
        val attrValueCount = 200
        val schemaUrl = "https://example.com/schema"

        val cfg = LoggerProviderConfigImpl(clock).apply {
            export { createCompositeLogRecordProcessor(listOf(firstProcessor, secondProcessor)) }

            resource(schemaUrl) {
                setStringAttribute("key", "value")
            }

            logLimits {
                attributeCountLimit = attrCount
                attributeValueLengthLimit = attrValueCount
            }
        }.generateLoggingConfig()

        assertNotNull(cfg.processors.single())
        assertEquals(schemaUrl, cfg.resource.schemaUrl)
        assertEquals(mapOf("key" to "value"), cfg.resource.attributes)

        with(cfg.logLimits) {
            assertEquals(attrCount, attributeCountLimit)
            assertEquals(attrValueCount, attributeValueLengthLimit)
        }
    }

    @Test
    fun testDoubleExportConfig() {
        assertFailsWith(IllegalArgumentException::class) {
            LoggerProviderConfigImpl(clock).apply {
                export { simpleLogRecordProcessor(stdoutLogRecordExporter()) }
                export { simpleLogRecordProcessor(stdoutLogRecordExporter()) }
            }
        }
    }

    @Test
    fun testResourceOverride() {
        val cfg = LoggerProviderConfigImpl(clock).apply {
            resource(mapOf("extra" to true))
        }.generateLoggingConfig()
        assertEquals(mapOf("extra" to true), cfg.resource.attributes)
    }

    @Test
    fun testSimpleResourceConfig() {
        val cfg = LoggerProviderConfigImpl(clock).apply {
            resource(mapOf("key" to "value"))
        }.generateLoggingConfig()
        assertEquals(mapOf("key" to "value"), cfg.resource.attributes)
    }

    @Test
    fun testResourceLimit() {
        val attrs = (0..DEFAULT_ATTRIBUTE_LIMIT + 2).associate {
            "key$it" to "value$it"
        }
        val cfg = LoggerProviderConfigImpl(clock).apply {
            resource(attrs)
        }.generateLoggingConfig()
        assertEquals(DEFAULT_ATTRIBUTE_LIMIT, cfg.resource.attributes.size)
    }
}
