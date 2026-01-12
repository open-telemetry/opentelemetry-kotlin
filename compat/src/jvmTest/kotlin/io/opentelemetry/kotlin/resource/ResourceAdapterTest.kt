package io.opentelemetry.kotlin.resource

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaResource
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalApi::class)
internal class ResourceAdapterTest {

    @Test
    fun testResourceAdapter() {
        val schemaUrl = "https://example.com/schema"
        val impl = OtelJavaResource.builder().put("key", "value").setSchemaUrl(schemaUrl).build()
        val adapter = ResourceAdapter(impl)
        assertEquals(mapOf("key" to "value"), adapter.attributes)
        assertEquals(schemaUrl, adapter.schemaUrl)

        val newResource = adapter.asNewResource {
            this.attributes.put("extra", "value")
        }
        assertEquals(mapOf("key" to "value", "extra" to "value"), newResource.attributes)
    }
}
