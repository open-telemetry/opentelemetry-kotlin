package io.opentelemetry.kotlin.tracing

import kotlin.test.Test
import kotlin.test.assertFailsWith

internal class SpanReadExtTest {

    @Test
    fun toReadableSpanThrows() {
        val span = FakeSpan()
        assertFailsWith<ClassCastException> {
            span.toReadableSpan()
        }
    }
}
