package io.opentelemetry.kotlin.init

import io.opentelemetry.kotlin.assertHasSdkDefaultAttributes
import io.opentelemetry.kotlin.attributes.AttributesModel
import io.opentelemetry.kotlin.attributes.DEFAULT_ATTRIBUTE_LIMIT
import io.opentelemetry.kotlin.clock.FakeClock
import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.error.NoopSdkErrorHandler
import io.opentelemetry.kotlin.factory.ContextFactoryImpl
import io.opentelemetry.kotlin.factory.FakeSpanFactory
import io.opentelemetry.kotlin.factory.IdGeneratorImpl
import io.opentelemetry.kotlin.factory.SpanContextFactoryImpl
import io.opentelemetry.kotlin.factory.SpanFactoryImpl
import io.opentelemetry.kotlin.factory.TraceFlagsFactoryImpl
import io.opentelemetry.kotlin.factory.TraceStateFactoryImpl
import io.opentelemetry.kotlin.sdkDefaultAttributes
import io.opentelemetry.kotlin.semconv.ServiceAttributes
import io.opentelemetry.kotlin.semconv.TelemetryAttributes
import io.opentelemetry.kotlin.tracing.NonRecordingSpan
import io.opentelemetry.kotlin.tracing.SpanKind
import io.opentelemetry.kotlin.tracing.TraceFlagsImpl
import io.opentelemetry.kotlin.tracing.export.FakeSpanProcessor
import io.opentelemetry.kotlin.tracing.export.SpanProcessor
import io.opentelemetry.kotlin.tracing.export.compositeSpanProcessor
import io.opentelemetry.kotlin.tracing.sampling.FakeSampler
import io.opentelemetry.kotlin.tracing.sampling.ParentBasedSampler
import io.opentelemetry.kotlin.tracing.sampling.Sampler
import io.opentelemetry.kotlin.tracing.sampling.SamplingResult.Decision
import io.opentelemetry.kotlin.tracing.sampling.alwaysOn
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNotSame
import kotlin.test.assertNull
import kotlin.test.assertSame

internal class TracerProviderConfigImplTest {

    private val clock = FakeClock()
    private val base = sdkDefaultResource()

    private val traceFlagsFactory = TraceFlagsFactoryImpl()
    private val traceStateFactory = TraceStateFactoryImpl()
    private val spanContextFactory =
        SpanContextFactoryImpl(IdGeneratorImpl(), traceFlagsFactory, traceStateFactory)
    private val contextFactory = ContextFactoryImpl(SpanFactoryImpl(spanContextFactory))

    @Test
    fun testDefaultSamplerParentBased() {
        val cfg = TracerProviderConfigImpl(clock, NoopSdkErrorHandler).generateTracingConfig(base)
        val sampler = assertIs<ParentBasedSampler>(cfg.samplerFactory(FakeSpanFactory()))
        assertContains(sampler.description, "root:AlwaysOnSampler")
    }

    @Test
    fun testDefaultSamplerRootSamples() {
        assertEquals(Decision.RECORD_AND_SAMPLE, defaultSampler().decisionFor(contextFactory.root()))
    }

    @Test
    fun testDefaultSamplerRemoteParentSampled() {
        val context = contextWithParent(sampled = true, isRemote = true)
        assertEquals(Decision.RECORD_AND_SAMPLE, defaultSampler().decisionFor(context))
    }

    @Test
    fun testDefaultSamplerRemoteParentNotSampled() {
        val context = contextWithParent(sampled = false, isRemote = true)
        assertEquals(Decision.DROP, defaultSampler().decisionFor(context))
    }

    @Test
    fun testDefaultSamplerLocalParentSampled() {
        val context = contextWithParent(sampled = true, isRemote = false)
        assertEquals(Decision.RECORD_AND_SAMPLE, defaultSampler().decisionFor(context))
    }

    @Test
    fun testDefaultSamplerLocalParentNotSampled() {
        val context = contextWithParent(sampled = false, isRemote = false)
        assertEquals(Decision.DROP, defaultSampler().decisionFor(context))
    }

    @Test
    fun testBuiltInSamplerConfig() {
        val cfg = TracerProviderConfigImpl(clock, NoopSdkErrorHandler).apply {
            sampler { alwaysOn() }
        }.generateTracingConfig(base)
        assertNotNull(cfg.samplerFactory(FakeSpanFactory()))
    }

    @Test
    fun testCustomSamplerConfig() {
        val sampler = FakeSampler()
        val cfg = TracerProviderConfigImpl(clock, NoopSdkErrorHandler).apply {
            sampler {
                sampler
            }
        }.generateTracingConfig(base)
        assertSame(sampler, cfg.samplerFactory(FakeSpanFactory()))
    }

    @Test
    fun testDefaultTracingConfig() {
        val cfg = TracerProviderConfigImpl(clock, NoopSdkErrorHandler).generateTracingConfig(base)
        assertNull(cfg.processor)
        assertEquals(sdkDefaultAttributes, cfg.resource.attributes)
        assertNull(cfg.resource.schemaUrl)

        with(cfg.spanLimits) {
            assertEquals(128, linkCountLimit)
            assertEquals(128, eventCountLimit)
            assertEquals(128, attributeCountLimit)
            assertEquals(128, attributeCountPerLinkLimit)
            assertEquals(128, attributeCountPerEventLimit)
            assertEquals(Int.MAX_VALUE, attributeValueLengthLimit)
        }
    }

    @Test
    fun testSdkDefaultAttributes() {
        val cfg = TracerProviderConfigImpl(clock, NoopSdkErrorHandler).generateTracingConfig(base)
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
        val attrValueLength = 600
        val schemaUrl = "https://example.com/schema"

        val cfg = TracerProviderConfigImpl(clock, NoopSdkErrorHandler).apply {
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
                attributeValueLengthLimit = attrValueLength
            }
        }.generateTracingConfig(base)

        assertNotNull(cfg.processor)
        assertEquals(schemaUrl, cfg.resource.schemaUrl)
        assertEquals(sdkDefaultAttributes + mapOf("key" to "value"), cfg.resource.attributes)

        with(cfg.spanLimits) {
            assertEquals(linkCount, linkCountLimit)
            assertEquals(eventCount, eventCountLimit)
            assertEquals(attrCount, attributeCountLimit)
            assertEquals(attrCountPerLink, attributeCountPerLinkLimit)
            assertEquals(attrCountPerEvent, attributeCountPerEventLimit)
            assertEquals(attrValueLength, attributeValueLengthLimit)
        }
    }

    @Test
    fun testDoubleExportConfigKeepsFirst() {
        var first: SpanProcessor? = null
        var second: SpanProcessor? = null
        val cfg = TracerProviderConfigImpl(clock, NoopSdkErrorHandler).apply {
            export {
                compositeSpanProcessor(FakeSpanProcessor()).apply {
                    first = this
                }
            }
            export {
                compositeSpanProcessor(FakeSpanProcessor()).apply {
                    second = this
                }
            }
        }.generateTracingConfig(base)
        assertSame(first, cfg.processor)
        assertNotSame(second, cfg.processor)
    }

    @Test
    fun testResourceOverride() {
        val cfg = TracerProviderConfigImpl(clock, NoopSdkErrorHandler).apply {
            resource(mapOf("extra" to true))
        }.generateTracingConfig(base)
        assertEquals(sdkDefaultAttributes + mapOf("extra" to true), cfg.resource.attributes)
    }

    @Test
    fun testSimpleResourceConfig() {
        val cfg = TracerProviderConfigImpl(clock, NoopSdkErrorHandler).apply {
            resource(mapOf("key" to "value"))
        }.generateTracingConfig(base)
        assertEquals(sdkDefaultAttributes + mapOf("key" to "value"), cfg.resource.attributes)
    }

    @Test
    fun testNoResourceLimit() {
        val count = DEFAULT_ATTRIBUTE_LIMIT + 3
        val attrs = (0 until count).associate { "key$it" to "value$it" }
        val cfg = TracerProviderConfigImpl(clock, NoopSdkErrorHandler).apply {
            resource(attrs)
        }.generateTracingConfig(base)
        assertEquals(count + sdkDefaultAttributes.size, cfg.resource.attributes.size)
    }

    @Test
    fun testSdkDefaultAttributes2() {
        val value = "my-custom-sdk"
        val cfg = TracerProviderConfigImpl(clock, NoopSdkErrorHandler).apply {
            resource(mapOf(TelemetryAttributes.TELEMETRY_SDK_NAME to value))
        }.generateTracingConfig(base)
        assertEquals(value, cfg.resource.attributes[TelemetryAttributes.TELEMETRY_SDK_NAME])
    }

    @Test
    fun testServiceNameDefaults() {
        val value = "my-service"
        val cfg = TracerProviderConfigImpl(clock, NoopSdkErrorHandler).apply {
            resource(mapOf(ServiceAttributes.SERVICE_NAME to value))
        }.generateTracingConfig(base)
        assertEquals(value, cfg.resource.attributes[ServiceAttributes.SERVICE_NAME])
    }

    @Test
    fun testServiceNameOverride() {
        val value = "my-service"
        val cfg = TracerProviderConfigImpl(clock, NoopSdkErrorHandler).apply {
            serviceName = value
        }.generateTracingConfig(base)
        assertEquals(value, cfg.resource.attributes[ServiceAttributes.SERVICE_NAME])
    }

    @Test
    fun testServiceNamePrecedence() {
        val value = "custom"
        val cfg = TracerProviderConfigImpl(clock, NoopSdkErrorHandler).apply {
            resource(mapOf(ServiceAttributes.SERVICE_NAME to "res"))
            serviceName = value
        }.generateTracingConfig(base)
        assertEquals(value, cfg.resource.attributes[ServiceAttributes.SERVICE_NAME])
    }

    private fun defaultSampler(): Sampler =
        TracerProviderConfigImpl(clock, NoopSdkErrorHandler).generateTracingConfig(base).samplerFactory(FakeSpanFactory())

    private fun contextWithParent(sampled: Boolean, isRemote: Boolean): Context {
        val traceFlags = when {
            sampled -> traceFlagsFactory.default
            else -> TraceFlagsImpl(isSampled = false, isRandom = false)
        }
        val parentSpanContext = spanContextFactory.create(
            traceId = "12345678901234567890123456789012",
            spanId = "1234567890123456",
            traceFlags = traceFlags,
            traceState = traceStateFactory.default,
            isRemote = isRemote,
        )
        val parentSpan = NonRecordingSpan(spanContextFactory.invalid, parentSpanContext)
        return contextFactory.root().storeSpan(parentSpan)
    }

    private fun Sampler.decisionFor(context: Context): Decision = shouldSample(
        context = context,
        traceId = "12345678901234567890123456789012",
        name = "span",
        spanKind = SpanKind.INTERNAL,
        attributes = AttributesModel(),
        links = emptyList(),
    ).decision
}
