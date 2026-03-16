package io.opentelemetry.kotlin.resource

import io.opentelemetry.api.common.AttributeKey
import io.opentelemetry.kotlin.aliases.OtelJavaResource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

internal class ResourceAdapterTest {

    @Test
    fun testResourceAdapter() {
        val schemaUrl = "https://example.com/schema"
        val impl = OtelJavaResource.builder().put("key", "value").setSchemaUrl(schemaUrl).build()
        val adapter = ResourceAdapter(impl)
        assertEquals(mapOf("key" to "value"), adapter.attributes)
        assertEquals(schemaUrl, adapter.schemaUrl)

        val newResource = adapter.asNewResource {
            this.attributes["extra"] = "value"
        }
        assertEquals(mapOf("key" to "value", "extra" to "value"), newResource.attributes)
    }

    @Test
    fun testMerge() {
        val noSchema = ResourceAdapter(OtelJavaResource.builder().build())
        val base = ResourceAdapter(
            OtelJavaResource.builder().put("a", "1").put("shared", "base")
                .setSchemaUrl("https://base.com").build()
        )
        val other = ResourceAdapter(
            OtelJavaResource.builder().put("b", "2").put("shared", "other")
                .setSchemaUrl("https://other.com").build()
        )

        val merged = base.merge(other)
        assertEquals("1", merged.attributes["a"])
        assertEquals("2", merged.attributes["b"])
        assertEquals("other", merged.attributes["shared"])
        assertEquals("https://other.com", merged.schemaUrl)

        assertNull(noSchema.merge(noSchema).schemaUrl)
        assertEquals("https://other.com", noSchema.merge(other).schemaUrl)
        assertEquals("https://base.com", base.merge(noSchema).schemaUrl)
    }

    @Test
    fun testAsNewResource() {
        val impl = OtelJavaResource.builder()
            .put("str", "hello")
            .put("long", 42L)
            .put("double", 3.14)
            .put("bool", true)
            .put(AttributeKey.stringArrayKey("strList"), listOf("a", "b"))
            .put(AttributeKey.longArrayKey("longList"), listOf(1L, 2L))
            .put(AttributeKey.doubleArrayKey("doubleList"), listOf(1.0, 2.0))
            .put(AttributeKey.booleanArrayKey("boolList"), listOf(true, false))
            .build()

        val result = ResourceAdapter(impl).asNewResource {}
        val attrs = result.attributes
        assertEquals("hello", attrs["str"])
        assertEquals(42L, attrs["long"])
        assertEquals(3.14, attrs["double"])
        assertEquals(true, attrs["bool"])
        assertEquals(listOf("a", "b"), attrs["strList"])
        assertEquals(listOf(1L, 2L), attrs["longList"])
        assertEquals(listOf(1.0, 2.0), attrs["doubleList"])
        assertEquals(listOf(true, false), attrs["boolList"])
    }
}
