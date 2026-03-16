package io.opentelemetry.kotlin.init

import io.opentelemetry.kotlin.assertHasSdkDefaultAttributes
import io.opentelemetry.kotlin.attributes.DEFAULT_ATTRIBUTE_LIMIT
import io.opentelemetry.kotlin.clock.FakeClock
import io.opentelemetry.kotlin.sdkDefaultAttributes
import io.opentelemetry.kotlin.semconv.TelemetryAttributes
import io.opentelemetry.kotlin.tracing.export.FakeSpanProcessor
import io.opentelemetry.kotlin.tracing.export.compositeSpanProcessor
import io.opentelemetry.kotlin.tracing.export.simpleSpanProcessor
import io.opentelemetry.kotlin.tracing.export.stdoutSpanExporter
import io.opentelemetry.kotlin.tracing.sampling.AlwaysOnSampler
import io.opentelemetry.kotlin.tracing.sampling.BuiltInSampler
import io.opentelemetry.kotlin.tracing.sampling.FakeSampler
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

internal class TracerProviderConfigImplTest {

    private val clock = FakeClock()

    @Test
    fun testDefaultSamplerAlwaysOn() {
        val cfg = TracerProviderConfigImpl(clock).generateTracingConfig()
        assertSame(AlwaysOnSampler, cfg.sampler)
    }

    @Test
    fun testBuiltInSamplerConfig() {
        val cfg = TracerProviderConfigImpl(clock).apply {
            sampler(BuiltInSampler.ALWAYS_ON)
        }.generateTracingConfig()
        assertNotNull(cfg.sampler)
    }

    @Test
    fun testCustomSamplerConfig() {
        val sampler = FakeSampler()
        val cfg = TracerProviderConfigImpl(clock).apply {
            sampler {
                sampler
            }
        }.generateTracingConfig()
        assertSame(sampler, cfg.sampler)
    }

    @Test
    fun testDefaultTracingConfig() {
        val cfg = TracerProviderConfigImpl(clock).generateTracingConfig()
        assertTrue(cfg.processors.isEmpty())
        assertEquals(sdkDefaultAttributes, cfg.resource.attributes)
        assertNull(cfg.resource.schemaUrl)

        with(cfg.spanLimits) {
            assertEquals(128, linkCountLimit)
            assertEquals(128, eventCountLimit)
            assertEquals(128, attributeCountLimit)
            assertEquals(128, attributeCountPerLinkLimit)
            assertEquals(128, attributeCountPerEventLimit)
        }
    }

    @Test
    fun testSdkDefaultAttributes() {
        val cfg = TracerProviderConfigImpl(clock).generateTracingConfig()
        assertHasSdkDefaultAttributes(cfg.resource.attributes)
    }

    @Test
    fun testOverrideTracingConfig() {
        val firstProcessor = FakeSpanProcessor()
        val secondProcessor = FakeSpanProcessor()
        val linkCount = 100
        val eventCount = 200
        val attrCount = 300
        val attrCountPerLink = 400
        val attrCountPerEvent = 500
        val schemaUrl = "https://example.com/schema"

        val cfg = TracerProviderConfigImpl(clock).apply {
            export { compositeSpanProcessor(firstProcessor, secondProcessor) }

            resource(schemaUrl) {
                setStringAttribute("key", "value")
            }

            spanLimits {
                linkCountLimit = linkCount
                eventCountLimit = eventCount
                attributeCountLimit = attrCount
                attributeCountPerLinkLimit = attrCountPerLink
                attributeCountPerEventLimit = attrCountPerEvent
            }
        }.generateTracingConfig()

        assertNotNull(cfg.processors.single())
        assertEquals(schemaUrl, cfg.resource.schemaUrl)
        assertEquals(sdkDefaultAttributes + mapOf("key" to "value"), cfg.resource.attributes)

        with(cfg.spanLimits) {
            assertEquals(linkCount, linkCountLimit)
            assertEquals(eventCount, eventCountLimit)
            assertEquals(attrCount, attributeCountLimit)
            assertEquals(attrCountPerLink, attributeCountPerLinkLimit)
            assertEquals(attrCountPerEvent, attributeCountPerEventLimit)
        }
    }

    @Test
    fun testDoubleExportConfig() {
        assertFailsWith(IllegalArgumentException::class) {
            TracerProviderConfigImpl(clock).apply {
                export { simpleSpanProcessor(stdoutSpanExporter()) }
                export { simpleSpanProcessor(stdoutSpanExporter()) }
            }
        }
    }

    @Test
    fun testResourceOverride() {
        val cfg = TracerProviderConfigImpl(clock).apply {
            resource(mapOf("extra" to true))
        }.generateTracingConfig()
        assertEquals(sdkDefaultAttributes + mapOf("extra" to true), cfg.resource.attributes)
    }

    @Test
    fun testSimpleResourceConfig() {
        val cfg = TracerProviderConfigImpl(clock).apply {
            resource(mapOf("key" to "value"))
        }.generateTracingConfig()
        assertEquals(sdkDefaultAttributes + mapOf("key" to "value"), cfg.resource.attributes)
    }

    @Test
    fun testResourceLimit() {
        val attrs = (0..DEFAULT_ATTRIBUTE_LIMIT + 2).associate {
            "key$it" to "value$it"
        }
        val cfg = TracerProviderConfigImpl(clock).apply {
            resource(attrs)
        }.generateTracingConfig()
        assertEquals(DEFAULT_ATTRIBUTE_LIMIT, cfg.resource.attributes.size)
    }

    @Test
    fun testUserAttributeOverridesSdkDefault() {
        val cfg = TracerProviderConfigImpl(clock).apply {
            resource(mapOf(TelemetryAttributes.TELEMETRY_SDK_NAME to "my-custom-sdk"))
        }.generateTracingConfig()
        assertEquals("my-custom-sdk", cfg.resource.attributes[TelemetryAttributes.TELEMETRY_SDK_NAME])
    }
}
