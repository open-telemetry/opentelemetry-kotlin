package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.attributes.AttributesMutator
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertSame

internal class OtelJavaMeterBuilderAdapterTest {

    @Test
    fun `setSchemaUrl returns this for chaining`() {
        val builder = OtelJavaMeterBuilderAdapter(CapturingMeterProvider(), "scope")
        assertSame(builder, builder.setSchemaUrl("https://example.com/schema"))
    }

    @Test
    fun `setInstrumentationVersion returns this for chaining`() {
        val builder = OtelJavaMeterBuilderAdapter(CapturingMeterProvider(), "scope")
        assertSame(builder, builder.setInstrumentationVersion("0.1.0"))
    }

    @Test
    fun `build calls getMeter with scope name`() {
        val provider = CapturingMeterProvider()
        OtelJavaMeterBuilderAdapter(provider, "my-scope").build()
        assertEquals("my-scope", provider.capturedName)
    }

    @Test
    fun `build passes null version and schemaUrl when unset`() {
        val provider = CapturingMeterProvider()
        OtelJavaMeterBuilderAdapter(provider, "scope").build()
        assertNull(provider.capturedVersion)
        assertNull(provider.capturedSchemaUrl)
    }

    @Test
    fun `build passes configured version`() {
        val provider = CapturingMeterProvider()
        OtelJavaMeterBuilderAdapter(provider, "scope")
            .setInstrumentationVersion("0.1.0")
            .build()
        assertEquals("0.1.0", provider.capturedVersion)
    }

    @Test
    fun `build passes configured schemaUrl`() {
        val provider = CapturingMeterProvider()
        OtelJavaMeterBuilderAdapter(provider, "scope")
            .setSchemaUrl("https://example.com/schema")
            .build()
        assertEquals("https://example.com/schema", provider.capturedSchemaUrl)
    }

    @Test
    fun `build passes both version and schemaUrl when both set`() {
        val provider = CapturingMeterProvider()
        OtelJavaMeterBuilderAdapter(provider, "scope")
            .setInstrumentationVersion("0.1.0")
            .setSchemaUrl("https://example.com/schema")
            .build()
        assertEquals("0.1.0", provider.capturedVersion)
        assertEquals("https://example.com/schema", provider.capturedSchemaUrl)
    }

    private class CapturingMeterProvider : MeterProvider {
        var capturedName: String? = null
        var capturedVersion: String? = null
        var capturedSchemaUrl: String? = null

        override fun getMeter(
            name: String,
            version: String?,
            schemaUrl: String?,
            attributes: (AttributesMutator.() -> Unit)?
        ): Meter {
            capturedName = name
            capturedVersion = version
            capturedSchemaUrl = schemaUrl
            return object : Meter {}
        }
    }
}
