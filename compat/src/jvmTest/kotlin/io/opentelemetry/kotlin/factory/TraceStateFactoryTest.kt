package io.opentelemetry.kotlin.factory

import io.opentelemetry.kotlin.ExperimentalApi
import org.junit.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalApi::class)
internal class TraceStateFactoryTest {

    private val factory = createCompatSdkFactory().traceStateFactory

    @Test
    fun `test valid`() {
        assertEquals(emptyMap(), factory.default.asMap())
    }
}
