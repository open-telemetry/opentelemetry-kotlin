package io.opentelemetry.kotlin.init

import io.opentelemetry.kotlin.clock.FakeClock
import io.opentelemetry.kotlin.error.FakeSdkErrorHandler
import io.opentelemetry.kotlin.resource.FakeResourceDetector
import io.opentelemetry.kotlin.semconv.ServiceAttributes
import org.junit.Test
import kotlin.test.assertEquals

internal class CompatResourceDetectionConfigTest {

    private val clock = FakeClock()
    private val testKey = "test.key"

    @Test
    fun `detected attributes reach the global resource`() {
        val cfg = CompatOpenTelemetryConfig(clock)
        cfg.resourceDetection {
            detector(FakeResourceDetector(attributes = mapOf(testKey to "detected")))
        }

        assertEquals("detected", cfg.buildGlobalResource().attributes[testKey])
    }

    @Test
    fun `later detector wins on conflict`() {
        val cfg = CompatOpenTelemetryConfig(clock)
        cfg.resourceDetection {
            detector(FakeResourceDetector(name = "first", attributes = mapOf(testKey to "a")))
            detector(FakeResourceDetector(name = "second", attributes = mapOf(testKey to "b")))
        }

        assertEquals("b", cfg.buildGlobalResource().attributes[testKey])
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

        val resource = cfg.buildGlobalResource()
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

        assertEquals("detected", cfg.buildGlobalResource().attributes[testKey])
        assertEquals("Resource detector 'broken' failed", handler.userCodeErrors.single().message)
    }
}
