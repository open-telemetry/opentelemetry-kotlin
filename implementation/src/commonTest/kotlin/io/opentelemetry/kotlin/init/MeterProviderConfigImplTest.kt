package io.opentelemetry.kotlin.init

import io.opentelemetry.kotlin.assertHasSdkDefaultAttributes
import io.opentelemetry.kotlin.attributes.DEFAULT_ATTRIBUTE_LIMIT
import io.opentelemetry.kotlin.sdkDefaultAttributes
import io.opentelemetry.kotlin.semconv.ServiceAttributes
import io.opentelemetry.kotlin.semconv.TelemetryAttributes
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class MeterProviderConfigImplTest {

    private val base = sdkDefaultResource()

    @Test
    fun testDefaultMetricsConfig() {
        val cfg = MeterProviderConfigImpl().generateMetricsConfig(base)
        assertEquals(sdkDefaultAttributes, cfg.resource.attributes)
        assertNull(cfg.resource.schemaUrl)
    }

    @Test
    fun testSdkDefaultAttributes() {
        val cfg = MeterProviderConfigImpl().generateMetricsConfig(base)
        assertHasSdkDefaultAttributes(cfg.resource.attributes)
    }

    @Test
    fun testOverrideMetricsConfig() {
        val schemaUrl = "https://example.com/schema"

        val cfg = MeterProviderConfigImpl().apply {
            resource(schemaUrl) {
                setStringAttribute("key", "value")
            }
        }.generateMetricsConfig(base)

        assertEquals(schemaUrl, cfg.resource.schemaUrl)
        assertEquals(sdkDefaultAttributes + mapOf("key" to "value"), cfg.resource.attributes)
    }

    @Test
    fun testResourceOverride() {
        val cfg = MeterProviderConfigImpl().apply {
            resource(mapOf("extra" to true))
        }.generateMetricsConfig(base)
        assertEquals(sdkDefaultAttributes + mapOf("extra" to true), cfg.resource.attributes)
    }

    @Test
    fun testSimpleResourceConfig() {
        val cfg = MeterProviderConfigImpl().apply {
            resource(mapOf("key" to "value"))
        }.generateMetricsConfig(base)
        assertEquals(sdkDefaultAttributes + mapOf("key" to "value"), cfg.resource.attributes)
    }

    @Test
    fun testNoResourceLimit() {
        val count = DEFAULT_ATTRIBUTE_LIMIT + 3
        val attrs = (0 until count).associate { "key$it" to "value$it" }
        val cfg = MeterProviderConfigImpl().apply {
            resource(attrs)
        }.generateMetricsConfig(base)
        assertEquals(count + sdkDefaultAttributes.size, cfg.resource.attributes.size)
    }

    @Test
    fun testSdkDefaultAttributesOverride() {
        val value = "my-custom-sdk"
        val cfg = MeterProviderConfigImpl().apply {
            resource(mapOf(TelemetryAttributes.TELEMETRY_SDK_NAME to value))
        }.generateMetricsConfig(base)
        assertEquals(value, cfg.resource.attributes[TelemetryAttributes.TELEMETRY_SDK_NAME])
    }

    @Test
    fun testServiceNameDefaults() {
        val value = "my-service"
        val cfg = MeterProviderConfigImpl().apply {
            resource(mapOf(ServiceAttributes.SERVICE_NAME to value))
        }.generateMetricsConfig(base)
        assertEquals(value, cfg.resource.attributes[ServiceAttributes.SERVICE_NAME])
    }

    @Test
    fun testServiceNameOverride() {
        val value = "my-service"
        val cfg = MeterProviderConfigImpl().apply {
            serviceName = value
        }.generateMetricsConfig(base)
        assertEquals(value, cfg.resource.attributes[ServiceAttributes.SERVICE_NAME])
    }

    @Test
    fun testServiceNamePrecedence() {
        val value = "custom"
        val cfg = MeterProviderConfigImpl().apply {
            resource(mapOf(ServiceAttributes.SERVICE_NAME to "res"))
            serviceName = value
        }.generateMetricsConfig(base)
        assertEquals(value, cfg.resource.attributes[ServiceAttributes.SERVICE_NAME])
    }
}
