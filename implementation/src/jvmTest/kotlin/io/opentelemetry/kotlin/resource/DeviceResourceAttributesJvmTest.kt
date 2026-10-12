package io.opentelemetry.kotlin.resource

import kotlin.test.Test
import kotlin.test.assertTrue

internal class DeviceResourceAttributesJvmTest {

    @Test
    internal fun `does not detect device attributes on JVM`() {
        assertTrue(detectDeviceResourceAttributes().isEmpty)
    }
}
