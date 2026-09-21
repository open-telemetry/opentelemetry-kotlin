package io.opentelemetry.kotlin.init

import io.opentelemetry.kotlin.behavior.OpenTelemetryBehavior
import io.opentelemetry.kotlin.clock.FakeClock
import io.opentelemetry.kotlin.resource.FakeResourceDetector
import io.opentelemetry.kotlin.sdkDefaultSchemaUrl
import io.opentelemetry.kotlin.semconv.ServiceAttributes
import io.opentelemetry.kotlin.semconv.TelemetryAttributes
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

internal class ResourcePrecedenceOrderTest {

    private val clock = FakeClock()
    private val testKey = "test.key"
    private val customSchemaUrl = "https://example.com/schema"

    @Test
    fun testSdkDefaults() {
        val cfg = OpenTelemetryConfigImpl(clock)
        val resolver = SdkConfigFactory(cfg, OpenTelemetryBehavior())
        val tracing = resolver.generateTracingConfig()
        val logging = resolver.generateLoggingConfig()

        val traceAttrs = tracing.resource.attributes
        assertNotNull(traceAttrs[TelemetryAttributes.TELEMETRY_SDK_NAME])
        assertNotNull(traceAttrs[TelemetryAttributes.TELEMETRY_SDK_LANGUAGE])
        assertNotNull(traceAttrs[TelemetryAttributes.TELEMETRY_SDK_VERSION])

        val logAttrs = logging.resource.attributes
        assertNotNull(logAttrs[TelemetryAttributes.TELEMETRY_SDK_NAME])
        assertNotNull(logAttrs[TelemetryAttributes.TELEMETRY_SDK_LANGUAGE])
        assertNotNull(logAttrs[TelemetryAttributes.TELEMETRY_SDK_VERSION])
    }

    @Test
    fun testSdkDefaultSchemaUrl() {
        val schemaUrl = sdkDefaultResource().schemaUrl
        assertNotNull(schemaUrl)
        assertTrue(
            schemaUrl.startsWith("https://opentelemetry.io/schemas/"),
            "Invalid schema URL: $schemaUrl",
        )

        val cfg = OpenTelemetryConfigImpl(clock)
        val resolver = SdkConfigFactory(cfg, OpenTelemetryBehavior())
        assertEquals(schemaUrl, resolver.generateTracingConfig().resource.schemaUrl)
        assertEquals(schemaUrl, resolver.generateLoggingConfig().resource.schemaUrl)
        assertEquals(schemaUrl, resolver.generateMetricsConfig().resource.schemaUrl)
    }

    @Test
    fun testSdkDefaultSchemaUrlSurvivesGlobalResource() {
        val cfg = OpenTelemetryConfigImpl(clock)
        cfg.resource(mapOf(testKey to "top"))

        val resolver = SdkConfigFactory(cfg, OpenTelemetryBehavior())
        assertEquals(sdkDefaultSchemaUrl, resolver.generateTracingConfig().resource.schemaUrl)
        assertEquals(sdkDefaultSchemaUrl, resolver.generateLoggingConfig().resource.schemaUrl)
    }

    @Test
    fun testSchemaUrlOverrides() {
        val cfg = OpenTelemetryConfigImpl(clock)
        cfg.resource(customSchemaUrl) { }

        val resolver = SdkConfigFactory(cfg, OpenTelemetryBehavior())
        assertEquals(customSchemaUrl, resolver.generateTracingConfig().resource.schemaUrl)
        assertEquals(customSchemaUrl, resolver.generateLoggingConfig().resource.schemaUrl)
    }

    @Test
    fun testGlobalOverrides() {
        val cfg = OpenTelemetryConfigImpl(clock)
        cfg.resource(mapOf(TelemetryAttributes.TELEMETRY_SDK_NAME to "custom-sdk"))

        val resolver = SdkConfigFactory(cfg, OpenTelemetryBehavior())
        assertEquals(
            "custom-sdk",
            resolver.generateTracingConfig().resource.attributes[TelemetryAttributes.TELEMETRY_SDK_NAME]
        )
        assertEquals(
            "custom-sdk",
            resolver.generateLoggingConfig().resource.attributes[TelemetryAttributes.TELEMETRY_SDK_NAME]
        )
    }

    @Test
    fun testSpecificOverrides() {
        val cfg = OpenTelemetryConfigImpl(clock)
        cfg.tracerProvider {
            resource(mapOf(TelemetryAttributes.TELEMETRY_SDK_NAME to "tracer-sdk"))
        }

        val resolver = SdkConfigFactory(cfg, OpenTelemetryBehavior())
        assertEquals(
            "tracer-sdk",
            resolver.generateTracingConfig().resource.attributes[TelemetryAttributes.TELEMETRY_SDK_NAME]
        )
    }

    @Test
    fun testOverridePrecedence() {
        val cfg = OpenTelemetryConfigImpl(clock)
        cfg.resource(mapOf(testKey to "top"))
        cfg.tracerProvider {
            resource(mapOf(testKey to "provider"))
        }

        val resolver = SdkConfigFactory(cfg, OpenTelemetryBehavior())
        assertEquals("provider", resolver.generateTracingConfig().resource.attributes[testKey])
        assertEquals("top", resolver.generateLoggingConfig().resource.attributes[testKey])
    }

    @Test
    fun testOverridePrecedence2() {
        val cfg = OpenTelemetryConfigImpl(clock)
        cfg.resource(mapOf(testKey to "top"))
        cfg.tracerProvider {
            resource(mapOf(testKey to "tracer-only"))
        }

        val resolver = SdkConfigFactory(cfg, OpenTelemetryBehavior())
        assertEquals("tracer-only", resolver.generateTracingConfig().resource.attributes[testKey])
        assertEquals("top", resolver.generateLoggingConfig().resource.attributes[testKey])
    }

    @Test
    fun testDetectorOverridesSdkDefaults() {
        val cfg = OpenTelemetryConfigImpl(clock)
        cfg.resourceDetection {
            detector(FakeResourceDetector(attributes = mapOf(ServiceAttributes.SERVICE_NAME to "detected")))
        }

        val resolver = SdkConfigFactory(cfg, OpenTelemetryBehavior())
        assertEquals("detected", resolver.generateTracingConfig().resource.attributes[ServiceAttributes.SERVICE_NAME])
        assertEquals("detected", resolver.generateLoggingConfig().resource.attributes[ServiceAttributes.SERVICE_NAME])
        assertEquals("detected", resolver.generateMetricsConfig().resource.attributes[ServiceAttributes.SERVICE_NAME])
    }

    @Test
    fun testDetectorAppliesToAllSignals() {
        val cfg = OpenTelemetryConfigImpl(clock)
        cfg.resourceDetection {
            detector(FakeResourceDetector(attributes = mapOf(testKey to "detected")))
        }

        val resolver = SdkConfigFactory(cfg, OpenTelemetryBehavior())
        assertEquals("detected", resolver.generateTracingConfig().resource.attributes[testKey])
        assertEquals("detected", resolver.generateLoggingConfig().resource.attributes[testKey])
        assertEquals("detected", resolver.generateMetricsConfig().resource.attributes[testKey])
    }

    @Test
    fun testExplicitConfigOverridesDetector() {
        val cfg = OpenTelemetryConfigImpl(clock)
        cfg.resourceDetection {
            detector(FakeResourceDetector(attributes = mapOf(testKey to "detected")))
        }
        cfg.resource(mapOf(testKey to "top"))
        cfg.tracerProvider {
            resource(mapOf(testKey to "provider"))
        }

        val resolver = SdkConfigFactory(cfg, OpenTelemetryBehavior())
        assertEquals("provider", resolver.generateTracingConfig().resource.attributes[testKey])
        assertEquals("top", resolver.generateLoggingConfig().resource.attributes[testKey])
    }

    @Test
    fun testServiceNameOverridesDetector() {
        val cfg = OpenTelemetryConfigImpl(clock)
        cfg.resourceDetection {
            detector(FakeResourceDetector(attributes = mapOf(ServiceAttributes.SERVICE_NAME to "detected")))
        }
        cfg.serviceName = "explicit"

        val resolver = SdkConfigFactory(cfg, OpenTelemetryBehavior())
        assertEquals("explicit", resolver.generateTracingConfig().resource.attributes[ServiceAttributes.SERVICE_NAME])
    }

    @Test
    fun testDetectorRunsOncePerSdk() {
        val detector = FakeResourceDetector(attributes = mapOf(testKey to "detected"))
        val cfg = OpenTelemetryConfigImpl(clock)
        cfg.resourceDetection {
            detector(detector)
        }

        val resolver = SdkConfigFactory(cfg, OpenTelemetryBehavior())
        resolver.generateTracingConfig()
        resolver.generateLoggingConfig()
        resolver.generateMetricsConfig()

        assertEquals(1, detector.detectCount)
    }

    @Test
    fun testDetectorSchemaUrlIsUsed() {
        val schemaUrl = "https://opentelemetry.io/schemas/1.43.0"
        val cfg = OpenTelemetryConfigImpl(clock)
        cfg.resourceDetection {
            detector(FakeResourceDetector(attributes = mapOf(testKey to "detected"), schemaUrl = schemaUrl))
        }

        val resolver = SdkConfigFactory(cfg, OpenTelemetryBehavior())
        assertEquals(schemaUrl, resolver.generateTracingConfig().resource.schemaUrl)
    }
}
