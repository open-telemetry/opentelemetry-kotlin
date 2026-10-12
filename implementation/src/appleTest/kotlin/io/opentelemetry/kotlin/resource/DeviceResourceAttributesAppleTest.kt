package io.opentelemetry.kotlin.resource

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

internal class DeviceResourceAttributesAppleTest {

    @Test
    internal fun `detects Apple device attributes`() {
        val attributes = detectDeviceResourceAttributes()

        assertEquals("Apple", attributes.manufacturer)
        assertTrue(assertNotNull(attributes.modelIdentifier).isNotBlank())
        assertTrue(assertNotNull(attributes.modelName).isNotBlank())
    }
}
