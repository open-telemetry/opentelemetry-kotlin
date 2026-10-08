package io.opentelemetry.kotlin.resource

import io.opentelemetry.kotlin.semconv.IncubatingApi
import io.opentelemetry.kotlin.semconv.OsAttributes
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@OptIn(IncubatingApi::class)
internal class HostResourceAttributesAppleTest {

    @Test
    internal fun `detects Apple operating system attributes`() {
        val attributes = detectHostResourceAttributes()

        assertEquals(OsAttributes.OsTypeValues.DARWIN.value, attributes.osType)
        assertTrue(assertNotNull(attributes.osName).isNotBlank())
        assertTrue(assertNotNull(attributes.osVersion).isNotBlank())
    }
}
