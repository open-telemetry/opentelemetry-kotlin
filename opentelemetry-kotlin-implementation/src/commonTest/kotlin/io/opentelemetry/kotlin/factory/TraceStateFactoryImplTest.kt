package io.opentelemetry.kotlin.factory

import io.opentelemetry.kotlin.ExperimentalApi
import kotlin.test.Test
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalApi::class)
internal class TraceStateFactoryImplTest {

    private val factory = TraceStateFactoryImpl()

    @Test
    fun testDefaultTraceState() {
        val traceState = factory.default
        assertNull(traceState.get("any-key"))
        assertTrue(traceState.asMap().isEmpty())
    }
}
