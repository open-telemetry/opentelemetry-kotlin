package io.opentelemetry.kotlin.clock

import io.opentelemetry.kotlin.fakes.otel.java.FakeOtelJavaClock
import org.junit.Test
import kotlin.test.assertEquals

internal class ClockAdapterTest {

    @Test
    fun `test clock`() {
        val impl = FakeOtelJavaClock()
        val adapter = ClockAdapter(impl)
        assertEquals(impl.now(), adapter.now())
    }
}
