package io.opentelemetry.kotlin.factory

import org.junit.Test
import kotlin.test.assertEquals

internal class TraceStateFactoryTest {

    private val factory = CompatTraceStateFactory()

    @Test
    fun `test valid`() {
        assertEquals(emptyMap(), factory.default.asMap())
    }
}
