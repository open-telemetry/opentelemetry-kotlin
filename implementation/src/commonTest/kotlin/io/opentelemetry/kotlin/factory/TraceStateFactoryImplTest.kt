package io.opentelemetry.kotlin.factory

import kotlin.test.Test
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class TraceStateFactoryImplTest {

    private val factory = TraceStateFactoryImpl()

    @Test
    fun testDefaultTraceState() {
        val traceState = factory.default
        assertNull(traceState.get("any-key"))
        assertTrue(traceState.asMap().isEmpty())
    }
}
