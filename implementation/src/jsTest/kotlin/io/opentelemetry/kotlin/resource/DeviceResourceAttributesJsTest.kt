package io.opentelemetry.kotlin.resource

import kotlin.test.Test
import kotlin.test.assertTrue

internal class DeviceResourceAttributesJsTest {

    @Test
    internal fun `does not detect device attributes on JS`() {
        assertTrue(detectDeviceResourceAttributes().isEmpty)
    }
}
