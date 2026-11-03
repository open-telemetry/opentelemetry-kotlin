package io.opentelemetry.kotlin.config.yaml

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.config.schema.model.AttributeNameValue
import io.opentelemetry.kotlin.config.schema.model.AttributeType
import io.opentelemetry.kotlin.config.schema.model.Resource
import io.opentelemetry.kotlin.error.FakeSdkErrorHandler
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalApi::class)
internal class ResourceMapperTest {

    // -------------------------------------------------------------------------
    // attributes_list — valid cases
    // -------------------------------------------------------------------------

    @Test
    fun mapsValidAttributesList() {
        val resource = Resource(attributesList = "service.name=my-service,service.version=1.0.0")
        val behavior = resource.toBehavior()
        assertEquals(
            mapOf("service.name" to "my-service", "service.version" to "1.0.0"),
            behavior.attributes,
        )
    }

    @Test
    fun mapsAttributesListWithPercentEncodedValues() {
        val resource = Resource(attributesList = "key=hello%20world")
        val behavior = resource.toBehavior()
        assertEquals(mapOf("key" to "hello world"), behavior.attributes)
    }

    @Test
    fun mapsAttributesListWithValueContainingEquals() {
        // Values are split on the FIRST '=' only, so '=' inside a value is allowed.
        val resource = Resource(attributesList = "key=a=b=c")
        val behavior = resource.toBehavior()
        assertEquals(mapOf("key" to "a=b=c"), behavior.attributes)
    }

    @Test
    fun mapsAttributesListIgnoringEmptyEntries() {
        val resource = Resource(attributesList = "k1=v1,,k2=v2,")
        val behavior = resource.toBehavior()
        assertEquals(mapOf("k1" to "v1", "k2" to "v2"), behavior.attributes)
    }

    @Test
    fun mapsSchemaUrl() {
        val resource = Resource(schemaUrl = "https://opentelemetry.io/schemas/1.37.0")
        val behavior = resource.toBehavior()
        assertEquals("https://opentelemetry.io/schemas/1.37.0", behavior.schemaUrl)
    }

    // -------------------------------------------------------------------------
    // attributes_list — error cases
    // -------------------------------------------------------------------------

    @Test
    fun discardsEntireAttributesListOnMalformedEntry() {
        val handler = FakeSdkErrorHandler()
        // "no-equals" has no '=' so it is malformed
        val resource = Resource(attributesList = "good=v,no-equals,other=v2")
        val behavior = resource.toBehavior(handler)

        assertNull(behavior.attributes)
        assertEquals(1, handler.apiMisuses.size)
        assertEquals("resource.attributes_list", handler.apiMisuses.single().api)
    }

    @Test
    fun discardsEntireAttributesListOnEmptyKeyBeforeEquals() {
        val handler = FakeSdkErrorHandler()
        val resource = Resource(attributesList = "=value")
        val behavior = resource.toBehavior(handler)

        assertNull(behavior.attributes)
        assertEquals(1, handler.apiMisuses.size)
        assertEquals("resource.attributes_list", handler.apiMisuses.single().api)
    }

    @Test
    fun discardsEntireAttributesListOnMalformedPercentEncoding() {
        val handler = FakeSdkErrorHandler()
        val resource = Resource(attributesList = "key=%ZZ")
        val behavior = resource.toBehavior(handler)

        assertNull(behavior.attributes)
        assertEquals(1, handler.apiMisuses.size)
        assertEquals("resource.attributes_list", handler.apiMisuses.single().api)
    }

    @Test
    fun reportsNoErrorForNullAttributesList() {
        val handler = FakeSdkErrorHandler()
        val resource = Resource(attributesList = null)
        resource.toBehavior(handler)

        assertTrue(handler.errors.isEmpty())
    }

    // -------------------------------------------------------------------------
    // attributes[] — valid cases
    // -------------------------------------------------------------------------

    @Test
    fun mapsStringAttribute() {
        val resource = Resource(
            attributes = listOf(AttributeNameValue(name = "env", value = "prod", type = AttributeType.STRING))
        )
        val behavior = resource.toBehavior()
        assertEquals(mapOf("env" to "prod"), behavior.attributes)
    }

    @Test
    fun mapsBoolAttribute() {
        val resource = Resource(
            attributes = listOf(AttributeNameValue(name = "debug", value = true, type = AttributeType.BOOL))
        )
        val behavior = resource.toBehavior()
        assertEquals(mapOf("debug" to true), behavior.attributes)
    }

    @Test
    fun mapsIntAttribute() {
        val resource = Resource(
            attributes = listOf(AttributeNameValue(name = "count", value = 42, type = AttributeType.INT))
        )
        val behavior = resource.toBehavior()
        assertEquals(mapOf("count" to 42L), behavior.attributes)
    }

    @Test
    fun mapsDoubleAttribute() {
        val resource = Resource(
            attributes = listOf(AttributeNameValue(name = "ratio", value = 0.5, type = AttributeType.DOUBLE))
        )
        val behavior = resource.toBehavior()
        assertEquals(mapOf("ratio" to 0.5), behavior.attributes)
    }

    @Test
    fun defaultsToStringTypeWhenTypeOmitted() {
        val resource = Resource(
            attributes = listOf(AttributeNameValue(name = "k", value = "v")) // no type field
        )
        val behavior = resource.toBehavior()
        assertEquals(mapOf("k" to "v"), behavior.attributes)
    }

    @Test
    fun ignoresAttributeEntryWithNullValue() {
        val resource = Resource(
            attributes = listOf(AttributeNameValue(name = "k", value = null, type = AttributeType.STRING))
        )
        val behavior = resource.toBehavior()
        assertNull(behavior.attributes)
    }

    // -------------------------------------------------------------------------
    // attributes[] — error cases
    // -------------------------------------------------------------------------

    @Test
    fun skipsAttributeEntryWithEmptyName() {
        val handler = FakeSdkErrorHandler()
        val resource = Resource(
            attributes = listOf(
                AttributeNameValue(name = "", value = "v", type = AttributeType.STRING),
                AttributeNameValue(name = "valid", value = "ok", type = AttributeType.STRING),
            )
        )
        val behavior = resource.toBehavior(handler)

        // The empty-name entry is skipped; the valid one is preserved.
        assertEquals(mapOf("valid" to "ok"), behavior.attributes)
        assertEquals(1, handler.apiMisuses.size)
        assertEquals("resource.attributes", handler.apiMisuses.single().api)
    }

    @Test
    fun skipsAttributeEntryWithTypeMismatch() {
        val handler = FakeSdkErrorHandler()
        val resource = Resource(
            attributes = listOf(
                // "not-a-bool" cannot be coerced to BOOL
                AttributeNameValue(name = "flag", value = "not-a-bool", type = AttributeType.BOOL),
                AttributeNameValue(name = "env", value = "prod", type = AttributeType.STRING),
            )
        )
        val behavior = resource.toBehavior(handler)

        // The invalid entry is skipped; the valid one is preserved.
        assertEquals(mapOf("env" to "prod"), behavior.attributes)
        assertEquals(1, handler.apiMisuses.size)
        assertEquals("resource.attributes", handler.apiMisuses.single().api)
    }

    @Test
    fun reportsErrorForEachInvalidAttributeEntry() {
        val handler = FakeSdkErrorHandler()
        val resource = Resource(
            attributes = listOf(
                AttributeNameValue(name = "", value = "x", type = AttributeType.STRING),
                AttributeNameValue(name = "bad-int", value = "abc", type = AttributeType.INT),
                AttributeNameValue(name = "good", value = "v", type = AttributeType.STRING),
            )
        )
        val behavior = resource.toBehavior(handler)

        assertEquals(mapOf("good" to "v"), behavior.attributes)
        assertEquals(2, handler.apiMisuses.size)
    }

    // -------------------------------------------------------------------------
    // Priority: attributes[] overrides attributes_list for the same key
    // -------------------------------------------------------------------------

    @Test
    fun typedAttributesOverrideAttributesListForSameKey() {
        val resource = Resource(
            attributesList = "service.name=from-list,region=us-east",
            attributes = listOf(
                AttributeNameValue(name = "service.name", value = "from-attrs", type = AttributeType.STRING)
            ),
        )
        val behavior = resource.toBehavior()

        // service.name from attributes[] wins; region from attributes_list is preserved.
        assertEquals("from-attrs", behavior.attributes?.get("service.name"))
        assertEquals("us-east", behavior.attributes?.get("region"))
    }

    // -------------------------------------------------------------------------
    // Wiring through OpenTelemetryConfiguration.toBehavior()
    // -------------------------------------------------------------------------

    @Test
    fun resourceSectionIsWiredThroughConfigurationMapper() {
        val handler = FakeSdkErrorHandler()
        val config = io.opentelemetry.kotlin.config.schema.model.OpenTelemetryConfiguration(
            fileFormat = "1.0",
            resource = Resource(
                attributesList = "k=v",
                schemaUrl = "https://example.com",
            )
        )
        val behavior = config.toBehavior(handler)

        assertEquals("https://example.com", behavior.resource?.schemaUrl)
        assertEquals(mapOf("k" to "v"), behavior.resource?.attributes)
        assertTrue(handler.errors.isEmpty())
    }

    @Test
    fun sdkErrorHandlerIsPassedThroughConfigurationMapper() {
        val handler = FakeSdkErrorHandler()
        val config = io.opentelemetry.kotlin.config.schema.model.OpenTelemetryConfiguration(
            fileFormat = "1.0",
            resource = Resource(attributesList = "bad-entry-no-equals"),
        )
        config.toBehavior(handler)

        assertEquals(1, handler.apiMisuses.size)
        assertEquals("resource.attributes_list", handler.apiMisuses.single().api)
    }
}
