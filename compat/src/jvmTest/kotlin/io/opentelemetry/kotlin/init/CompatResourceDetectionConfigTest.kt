package io.opentelemetry.kotlin.init

import io.opentelemetry.kotlin.SdkBuildKonfig
import io.opentelemetry.kotlin.behavior.OpenTelemetryBehavior
import io.opentelemetry.kotlin.clock.FakeClock
import io.opentelemetry.kotlin.error.FakeSdkErrorHandler
import io.opentelemetry.kotlin.factory.CompatContextFactory
import io.opentelemetry.kotlin.resource.FakeResourceDetector
import io.opentelemetry.kotlin.resource.SDK_DEFAULT_SERVICE_NAME
import io.opentelemetry.kotlin.resource.SdkMode
import io.opentelemetry.kotlin.resource.TELEMETRY_SDK_MODE
import io.opentelemetry.kotlin.semconv.SemconvBuildKonfig
import io.opentelemetry.kotlin.semconv.ServiceAttributes
import io.opentelemetry.kotlin.semconv.TelemetryAttributes
import org.junit.Test
import kotlin.test.assertEquals

internal class CompatResourceDetectionConfigTest {

    private val clock = FakeClock()
    private val testKey = "test.key"

    @Test
    fun `sdk defaults are present without any configuration`() {
        val resource = baseResource(CompatOpenTelemetryConfig(clock))
        assertEquals("opentelemetry", resource.attributes[TelemetryAttributes.TELEMETRY_SDK_NAME])
        assertEquals("kotlin", resource.attributes[TelemetryAttributes.TELEMETRY_SDK_LANGUAGE])
        assertEquals(SdkBuildKonfig.SDK_VERSION, resource.attributes[TelemetryAttributes.TELEMETRY_SDK_VERSION])
        assertEquals(SDK_DEFAULT_SERVICE_NAME, resource.attributes[ServiceAttributes.SERVICE_NAME])
        assertEquals(SdkMode.COMPAT.attributeValue, resource.attributes[TELEMETRY_SDK_MODE])
        assertEquals(SemconvBuildKonfig.SCHEMA_URL, resource.schemaUrl)
    }

    @Test
    fun `detected and explicit attributes override sdk defaults`() {
        val cfg = CompatOpenTelemetryConfig(clock)
        cfg.resourceDetection {
            detector(FakeResourceDetector(attributes = mapOf(ServiceAttributes.SERVICE_NAME to "detected")))
        }
        cfg.resource(mapOf(TelemetryAttributes.TELEMETRY_SDK_NAME to "custom-sdk"))

        val resource = baseResource(cfg)
        assertEquals("detected", resource.attributes[ServiceAttributes.SERVICE_NAME])
        assertEquals("custom-sdk", resource.attributes[TelemetryAttributes.TELEMETRY_SDK_NAME])
        assertEquals("kotlin", resource.attributes[TelemetryAttributes.TELEMETRY_SDK_LANGUAGE])
    }

    @Test
    fun `explicit schema url overrides sdk default`() {
        val cfg = CompatOpenTelemetryConfig(clock)
        cfg.resource("https://example.com/schema") { }
        assertEquals("https://example.com/schema", baseResource(cfg).schemaUrl)
    }

    @Test
    fun `detected attributes reach the global resource`() {
        val cfg = CompatOpenTelemetryConfig(clock)
        cfg.resourceDetection {
            detector(FakeResourceDetector(attributes = mapOf(testKey to "detected")))
        }

        assertEquals("detected", baseResource(cfg).attributes[testKey])
    }

    @Test
    fun `later detector wins on conflict`() {
        val cfg = CompatOpenTelemetryConfig(clock)
        cfg.resourceDetection {
            detector(FakeResourceDetector(name = "first", attributes = mapOf(testKey to "a")))
            detector(FakeResourceDetector(name = "second", attributes = mapOf(testKey to "b")))
        }

        assertEquals("b", baseResource(cfg).attributes[testKey])
    }

    @Test
    fun `explicit config overrides detected attributes`() {
        val cfg = CompatOpenTelemetryConfig(clock)
        cfg.resourceDetection {
            detector(
                FakeResourceDetector(
                    attributes = mapOf(
                        testKey to "detected",
                        ServiceAttributes.SERVICE_NAME to "detected",
                    ),
                )
            )
        }
        cfg.resource(mapOf(testKey to "explicit"))
        cfg.serviceName = "explicit"

        val resource = baseResource(cfg)
        assertEquals("explicit", resource.attributes[testKey])
        assertEquals("explicit", resource.attributes[ServiceAttributes.SERVICE_NAME])
    }

    @Test
    fun `throwing detector is reported and skipped`() {
        val handler = FakeSdkErrorHandler()
        val cfg = CompatOpenTelemetryConfig(clock)
        cfg.errorHandler(handler)
        cfg.resourceDetection {
            detector(FakeResourceDetector(name = "broken", error = IllegalStateException("boom")))
            detector(FakeResourceDetector(name = "working", attributes = mapOf(testKey to "detected")))
        }

        assertEquals("detected", baseResource(cfg).attributes[testKey])
        assertEquals("Resource detector 'broken' failed", handler.userCodeErrors.single().message)
    }

    private fun baseResource(cfg: CompatOpenTelemetryConfig) =
        CompatSdkConfigFactory(cfg, OpenTelemetryBehavior(), clock, CompatContextFactory()).baseResource
}
