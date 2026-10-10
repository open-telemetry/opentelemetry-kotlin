package io.opentelemetry.kotlin.config.envar

import io.opentelemetry.kotlin.behavior.ResourceBehavior
import io.opentelemetry.kotlin.config.envar.reader.EnvVarReadWarning
import io.opentelemetry.kotlin.config.envar.reader.reportingEnvVarReader
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class ResourceEnvVarsTest {

    @Test
    fun `should leave resource unset when environment configures nothing`() {
        assertNull(toBehavior(emptyMap()))
    }

    @Test
    fun `should read service name`() {
        val behavior = toBehavior(mapOf(ResourceEnvVars.SERVICE_NAME to "checkout"))

        assertEquals(ResourceBehavior(serviceName = "checkout"), behavior)
    }

    @Test
    fun `should read resource attributes`() {
        val behavior = toBehavior(
            mapOf(ResourceEnvVars.RESOURCE_ATTRIBUTES to "region=eu,service.version=1.2"),
        )

        assertEquals(
            ResourceBehavior(
                attributes = mapOf(
                    "region" to "eu",
                    "service.version" to "1.2",
                ),
            ),
            behavior,
        )
    }

    @Test
    fun `should decode percent-encoded keys and values`() {
        val behavior = toBehavior(
            mapOf(ResourceEnvVars.RESOURCE_ATTRIBUTES to "key%2Cwith%3Ddelimiters=hello%20world%2Cvalue%3D1"),
        )

        assertEquals(
            mapOf("key,with=delimiters" to "hello world,value=1"),
            behavior?.attributes,
        )
    }

    @Test
    fun `should trim optional whitespace around delimiters`() {
        val behavior = toBehavior(
            mapOf(ResourceEnvVars.RESOURCE_ATTRIBUTES to " region = eu , version = 1.2 "),
        )

        assertEquals(
            mapOf("region" to "eu", "version" to "1.2"),
            behavior?.attributes,
        )
    }

    @Test
    fun `should preserve encoded whitespace`() {
        val behavior = toBehavior(
            mapOf(ResourceEnvVars.RESOURCE_ATTRIBUTES to "key%20=%20value%20"),
        )

        assertEquals(mapOf("key " to " value "), behavior?.attributes)
    }

    @Test
    fun `should allow empty attribute values`() {
        val behavior = toBehavior(mapOf(ResourceEnvVars.RESOURCE_ATTRIBUTES to "key="))

        assertEquals(mapOf("key" to ""), behavior?.attributes)
    }

    @Test
    fun `should use the last value for a duplicate key`() {
        val behavior = toBehavior(
            mapOf(ResourceEnvVars.RESOURCE_ATTRIBUTES to "region=eu,region=us"),
        )

        assertEquals(mapOf("region" to "us"), behavior?.attributes)
    }

    @Test
    fun `should preserve service name from both environment variables`() {
        val behavior = toBehavior(
            mapOf(
                ResourceEnvVars.RESOURCE_ATTRIBUTES to "service.name=from-attributes",
                ResourceEnvVars.SERVICE_NAME to "from-service-name",
            ),
        )

        assertEquals("from-service-name", behavior?.serviceName)
        assertEquals("from-attributes", behavior?.attributes?.get("service.name"))
    }

    @Test
    fun `should discard every attribute and report a warning when one entry is invalid`() {
        INVALID_VALUES.forEach { value ->
            val warnings = mutableListOf<EnvVarReadWarning>()

            val behavior = toBehavior(
                env = mapOf(ResourceEnvVars.RESOURCE_ATTRIBUTES to value),
                onWarning = warnings::add,
            )

            assertNull(behavior, "<$value> should not configure a resource")
            assertEquals(
                listOf(
                    EnvVarReadWarning(
                        name = ResourceEnvVars.RESOURCE_ATTRIBUTES,
                        message = "Invalid resource attributes; ignoring",
                    ),
                ),
                warnings,
            )
        }
    }

    @Test
    fun `should preserve service name when resource attributes are invalid`() {
        val warnings = mutableListOf<EnvVarReadWarning>()

        val behavior = toBehavior(
            env = mapOf(
                ResourceEnvVars.RESOURCE_ATTRIBUTES to "valid=value,invalid",
                ResourceEnvVars.SERVICE_NAME to "checkout",
            ),
            onWarning = warnings::add,
        )

        assertEquals(ResourceBehavior(serviceName = "checkout"), behavior)
        assertEquals(ResourceEnvVars.RESOURCE_ATTRIBUTES, warnings.single().name)
    }

    private fun toBehavior(
        env: Map<String, String>,
        onWarning: (EnvVarReadWarning) -> Unit = {},
    ): ResourceBehavior? = ResourceEnvVars(
        reportingEnvVarReader(env::get, onWarning),
    ).toBehavior()

    private companion object {
        val INVALID_VALUES = listOf(
            "missing-value",
            "=missing-key",
            "key=value=with-unescaped-equals",
            "key=%ZZ",
            "valid=value,,another=valid",
        )
    }
}
