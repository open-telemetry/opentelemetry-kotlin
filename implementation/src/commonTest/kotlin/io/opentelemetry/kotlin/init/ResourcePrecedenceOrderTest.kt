package io.opentelemetry.kotlin.init

import io.opentelemetry.kotlin.clock.FakeClock
import io.opentelemetry.kotlin.semconv.TelemetryAttributes
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

internal class ResourcePrecedenceOrderTest {

    private val clock = FakeClock()
    private val testKey = "test.key"

    @Test
    fun testSdkDefaults() {
        val cfg = OpenTelemetryConfigImpl(clock)
        val tracing = cfg.generateTracingConfig()
        val logging = cfg.generateLoggingConfig()

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
    fun testGlobalOverrides() {
        val cfg = OpenTelemetryConfigImpl(clock)
        cfg.resource(mapOf(TelemetryAttributes.TELEMETRY_SDK_NAME to "custom-sdk"))

        assertEquals(
            "custom-sdk",
            cfg.generateTracingConfig().resource.attributes[TelemetryAttributes.TELEMETRY_SDK_NAME]
        )
        assertEquals(
            "custom-sdk",
            cfg.generateLoggingConfig().resource.attributes[TelemetryAttributes.TELEMETRY_SDK_NAME]
        )
    }

    @Test
    fun testSpecificOverrides() {
        val cfg = OpenTelemetryConfigImpl(clock)
        cfg.tracerProvider {
            resource(mapOf(TelemetryAttributes.TELEMETRY_SDK_NAME to "tracer-sdk"))
        }

        assertEquals(
            "tracer-sdk",
            cfg.generateTracingConfig().resource.attributes[TelemetryAttributes.TELEMETRY_SDK_NAME]
        )
    }

    @Test
    fun testOverridePrecedence() {
        val cfg = OpenTelemetryConfigImpl(clock)
        cfg.resource(mapOf(testKey to "top"))
        cfg.tracerProvider {
            resource(mapOf(testKey to "provider"))
        }

        assertEquals("provider", cfg.generateTracingConfig().resource.attributes[testKey])
        assertEquals("top", cfg.generateLoggingConfig().resource.attributes[testKey])
    }

    @Test
    fun testOverridePrecedence2() {
        val cfg = OpenTelemetryConfigImpl(clock)
        cfg.resource(mapOf(testKey to "top"))
        cfg.tracerProvider {
            resource(mapOf(testKey to "tracer-only"))
        }

        assertEquals("tracer-only", cfg.generateTracingConfig().resource.attributes[testKey])
        assertEquals("top", cfg.generateLoggingConfig().resource.attributes[testKey])
    }
}
