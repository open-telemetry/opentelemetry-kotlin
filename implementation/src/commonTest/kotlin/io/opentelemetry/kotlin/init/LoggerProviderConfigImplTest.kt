package io.opentelemetry.kotlin.init

import io.opentelemetry.kotlin.attributes.DEFAULT_ATTRIBUTE_LIMIT
import io.opentelemetry.kotlin.clock.FakeClock
import io.opentelemetry.kotlin.logging.export.FakeLogRecordProcessor
import io.opentelemetry.kotlin.logging.export.compositeLogRecordProcessor
import io.opentelemetry.kotlin.logging.export.simpleLogRecordProcessor
import io.opentelemetry.kotlin.logging.export.stdoutLogRecordExporter
import io.opentelemetry.kotlin.sdkDefaultAttributes
import io.opentelemetry.kotlin.semconv.ServiceAttributes
import io.opentelemetry.kotlin.semconv.TelemetryAttributes
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull

internal class LoggerProviderConfigImplTest {

    private val clock = FakeClock()
    private val base = sdkDefaultResource()

    @Test
    fun testDefaultLoggingConfig() {
        val cfg = LoggerProviderConfigImpl(clock).generateLoggingConfig(base)
        assertNull(cfg.processor)
        assertEquals(sdkDefaultAttributes, cfg.resource.attributes)
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
            export { compositeLogRecordProcessor(firstProcessor, secondProcessor) }

            resource(schemaUrl) {
                setStringAttribute("key", "value")
            }

            logLimits {
                attributeCountLimit = attrCount
                attributeValueLengthLimit = attrValueCount
            }
        }.generateLoggingConfig(base)

        assertNotNull(cfg.processor)
        assertEquals(schemaUrl, cfg.resource.schemaUrl)
        assertEquals(sdkDefaultAttributes + mapOf("key" to "value"), cfg.resource.attributes)

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
        }.generateLoggingConfig(base)
        assertEquals(sdkDefaultAttributes + mapOf("extra" to true), cfg.resource.attributes)
    }

    @Test
    fun testSimpleResourceConfig() {
        val cfg = LoggerProviderConfigImpl(clock).apply {
            resource(mapOf("key" to "value"))
        }.generateLoggingConfig(base)
        assertEquals(sdkDefaultAttributes + mapOf("key" to "value"), cfg.resource.attributes)
    }

    @Test
    fun testSdkDefaultAttributes() {
        val value = "my-custom-sdk"
        val cfg = LoggerProviderConfigImpl(clock).apply {
            resource(mapOf(TelemetryAttributes.TELEMETRY_SDK_NAME to value))
        }.generateLoggingConfig(base)
        assertEquals(value, cfg.resource.attributes[TelemetryAttributes.TELEMETRY_SDK_NAME])
    }

    @Test
    fun testServiceNameDefaults() {
        val value = "my-service"
        val cfg = LoggerProviderConfigImpl(clock).apply {
            resource(mapOf(ServiceAttributes.SERVICE_NAME to value))
        }.generateLoggingConfig(base)
        assertEquals(value, cfg.resource.attributes[ServiceAttributes.SERVICE_NAME])
    }

    @Test
    fun testNoResourceLimit() {
        val count = DEFAULT_ATTRIBUTE_LIMIT + 3
        val attrs = (0 until count).associate { "key$it" to "value$it" }
        val cfg = LoggerProviderConfigImpl(clock).apply {
            resource(attrs)
        }.generateLoggingConfig(base)
        assertEquals(count + sdkDefaultAttributes.size, cfg.resource.attributes.size)
    }
}
