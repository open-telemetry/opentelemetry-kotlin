package io.opentelemetry.kotlin.resource

import kotlin.test.Test
import kotlin.test.assertEquals

internal class DefaultServiceNameTest {

    @Test
    fun appendsExecutableName() {
        assertEquals("unknown_service:java", defaultServiceName("java"))
    }

    @Test
    fun fallsBackWhenExecutableNameIsMissing() {
        assertEquals("unknown_service", defaultServiceName(null))
        assertEquals("unknown_service", defaultServiceName(""))
        assertEquals("unknown_service", defaultServiceName(" "))
    }
}
