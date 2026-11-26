package io.opentelemetry.kotlin.resource

import io.opentelemetry.kotlin.attributes.AnyValue
import io.opentelemetry.kotlin.attributes.AttributeValue
import io.opentelemetry.kotlin.attributes.AttributesModel
import io.opentelemetry.kotlin.attributes.DEFAULT_ATTRIBUTE_LIMIT
import io.opentelemetry.kotlin.attributes.toAttributeValues
import io.opentelemetry.kotlin.error.SdkError
import io.opentelemetry.kotlin.error.SdkErrorHandler
import io.opentelemetry.kotlin.error.SdkErrorSeverity
import io.opentelemetry.kotlin.error.setProcessSdkErrorHandler
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotEquals
import kotlin.test.assertNull

internal class ResourceImplTest {

    private val errors = mutableListOf<SdkError>()

    @BeforeTest
    fun setUp() {
        setProcessSdkErrorHandler(null)
        check(setProcessSdkErrorHandler(SdkErrorHandler { errors += it }))
    }

    @AfterTest
    fun tearDown() {
        setProcessSdkErrorHandler(null)
    }

    @Test
    fun testResourceImpl() {
        val attrs =
            AttributesModel(attrs = typed(mapOf("key" to "value")))
        val resource = ResourceImpl(attrs, "https://example.com/schema")
        val another = resource.asNewResource {
            attributes["foo"] = "value"
            schemaUrl = "https://example.com/another"
        }

        assertEquals("value", resource.attributes["key"])
        assertEquals("value", another.attributes["key"])

        assertNull(resource.attributes["foo"])
        assertEquals("value", another.attributes["foo"])

        assertEquals("https://example.com/schema", resource.schemaUrl)
        assertEquals("https://example.com/another", another.schemaUrl)
    }

    @Test
    fun testEmptyResource() {
        val attrs =
            AttributesModel(attrs = typed(mapOf("key" to "value")))
        val resource = ResourceImpl(attrs, "https://example.com/schema")
        val another = resource.asNewResource {
            attributes.clear()
            schemaUrl = null
        }
        assertEquals(emptyMap(), another.attributes)
        assertNull(another.schemaUrl)
    }

    @Test
    fun testDefensiveCopy() {
        val container = AttributesModel()
        val resource = ResourceImpl(container, null)
        lateinit var attrs: MutableMap<String, Any>
        val another = resource.asNewResource {
            attrs = attributes
        }
        attrs["key"] = "value"
        assertEquals(emptyMap(), another.attributes)
    }

    @Test
    fun testNewResourceNoAttributeLimit() {
        val count = DEFAULT_ATTRIBUTE_LIMIT + 3
        val attrs = (0 until count).associate { "key$it" to "value$it" }
        val container = AttributesModel(attrs = typed(attrs))
        val resource = ResourceImpl(container, "https://example.com/schema")
        assertEquals(count, resource.attributes.size)
    }

    @Test
    fun testMutateResourceNoAttributeLimit() {
        val count = DEFAULT_ATTRIBUTE_LIMIT + 3
        val attrs = (0 until count).associate { "key$it" to "value$it" }
        val container = AttributesModel(attrs = typed(attrs))
        val resource = ResourceImpl(container, "https://example.com/schema")
        val mutated = resource.asNewResource {
            attributes["extraKey"] = "extraValue"
        }
        assertEquals(count + 1, mutated.attributes.size)
        assertEquals("extraValue", mutated.attributes["extraKey"])
    }

    @Test
    fun testMergeNonOverlapping() {
        val base = ResourceImpl(AttributesModel(attrs = typed(mapOf("a" to "1"))), "https://example.com/base")
        val other = ResourceImpl(AttributesModel(attrs = typed(mapOf("b" to "2"))), "https://example.com/other")
        val merged = base.merge(other)
        assertEquals("1", merged.attributes["a"])
        assertEquals("2", merged.attributes["b"])
    }

    @Test
    fun testMergeOtherWinsOnConflict() {
        val base = ResourceImpl(AttributesModel(attrs = typed(mapOf("key" to "base"))), null)
        val other = ResourceImpl(AttributesModel(attrs = typed(mapOf("key" to "other"))), null)
        val merged = base.merge(other)
        assertEquals("other", merged.attributes["key"])
    }

    @Test
    fun testMergeSchemaUrlBothNull() {
        val base = ResourceImpl(AttributesModel(), null)
        val other = ResourceImpl(AttributesModel(), null)
        assertNull(base.merge(other).schemaUrl)
    }

    @Test
    fun testMergeSchemaUrlBaseNull() {
        val base = ResourceImpl(AttributesModel(), null)
        val other = ResourceImpl(AttributesModel(), "https://example.com/other")
        assertEquals("https://example.com/other", base.merge(other).schemaUrl)
    }

    @Test
    fun testMergeSchemaUrlOtherNull() {
        val base = ResourceImpl(AttributesModel(), "https://example.com/base")
        val other = ResourceImpl(AttributesModel(), null)
        assertEquals("https://example.com/base", base.merge(other).schemaUrl)
    }

    @Test
    fun testMergeSchemaUrlSame() {
        val base = ResourceImpl(AttributesModel(), "https://example.com/schema")
        val other = ResourceImpl(AttributesModel(), "https://example.com/schema")
        assertEquals("https://example.com/schema", base.merge(other).schemaUrl)
    }

    @Test
    fun testMergeSchemaUrlDifferentOtherWins() {
        val base = ResourceImpl(AttributesModel(), "https://example.com/base")
        val other = ResourceImpl(AttributesModel(), "https://example.com/other")
        assertEquals("https://example.com/other", base.merge(other).schemaUrl)

        val error = assertIs<SdkError.ApiMisuse>(errors.single())
        assertEquals("Resource.merge", error.api)
        assertEquals(SdkErrorSeverity.WARNING, error.severity)
        assertEquals(
            "Conflicting schema URLs 'https://example.com/base' and 'https://example.com/other', " +
                "using 'https://example.com/other'.",
            error.message,
        )
    }

    @Test
    fun testMergeSchemaUrlWithoutConflictReportsNothing() {
        val none = ResourceImpl(AttributesModel(), null)
        val schema = ResourceImpl(AttributesModel(), "https://example.com/schema")
        none.merge(none)
        none.merge(schema)
        schema.merge(none)
        schema.merge(schema)
        assertEquals(emptyList(), errors)
    }

    @Test
    fun testMergeEmptySchemaUrlReportsNothing() {
        val empty = ResourceImpl(AttributesModel(), "")
        val schema = ResourceImpl(AttributesModel(), "https://example.com/schema")
        empty.merge(schema)
        schema.merge(empty)
        assertEquals(emptyList(), errors)
    }

    @Test
    fun testMergeNoAttributeLimit() {
        val baseAttrs = (0 until DEFAULT_ATTRIBUTE_LIMIT).associate { "base$it" to "v$it" }
        val otherAttrs = mapOf("extra" to "value")
        val base = ResourceImpl(AttributesModel(attrs = typed(baseAttrs)), null)
        val other = ResourceImpl(AttributesModel(attrs = typed(otherAttrs)), null)
        val merged = base.merge(other)
        assertEquals(DEFAULT_ATTRIBUTE_LIMIT + 1, merged.attributes.size)
        assertEquals("value", merged.attributes["extra"])
    }

    @Test
    fun testEqualsAndHashCodeByAttributesAndSchemaUrl() {
        val a = ResourceImpl(
            AttributesModel(attrs = typed(mapOf("k" to "v"))),
            "https://example.com/schema",
        )
        val b = ResourceImpl(
            AttributesModel(attrs = typed(mapOf("k" to "v"))),
            "https://example.com/schema",
        )
        val differentAttrs = ResourceImpl(
            AttributesModel(attrs = typed(mapOf("k" to "other"))),
            "https://example.com/schema",
        )
        val differentSchema = ResourceImpl(
            AttributesModel(attrs = typed(mapOf("k" to "v"))),
            "https://example.com/other",
        )

        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
        assertNotEquals(a, differentAttrs)
        assertNotEquals(a, differentSchema)
    }

    @Test
    fun testPreservesComplexAttributesOnMergeAndMutate() {
        val base = ResourceImpl(
            AttributesModel(
                attrs = typed(
                    mapOf(
                        "map" to mapOf("k" to "v"),
                        "listWithNull" to listOf("a", null),
                    )
                )
            ),
            null
        )
        val other = ResourceImpl(
            AttributesModel(
                attrs = typed(
                    mapOf(
                        "mixed" to listOf("x", 1L),
                    )
                )
            ),
            null
        )
        val merged = base.merge(other)
        assertEquals(
            AnyValue.MapValue(mapOf("k" to AnyValue.StringValue("v"))),
            merged.attributes["map"]
        )
        assertEquals(
            AnyValue.ListValue(listOf(AnyValue.StringValue("a"), AnyValue.NullValue)),
            merged.attributes["listWithNull"]
        )
        assertEquals(
            AnyValue.ListValue(listOf(AnyValue.StringValue("x"), AnyValue.LongValue(1L))),
            merged.attributes["mixed"]
        )

        val mutated = merged.asNewResource {
            attributes["addedMap"] = mapOf("inner" to 42L)
        }
        assertEquals(
            AnyValue.MapValue(mapOf("inner" to AnyValue.LongValue(42L))),
            mutated.attributes["addedMap"]
        )
    }

    private fun typed(attrs: Map<String, Any>): MutableMap<String, AttributeValue> =
        attrs.toAttributeValues().toMutableMap()
}
