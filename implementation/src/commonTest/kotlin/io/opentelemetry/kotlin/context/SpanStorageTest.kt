package io.opentelemetry.kotlin.context

import io.opentelemetry.kotlin.tracing.FakeSpan
import kotlin.test.Test
import kotlin.test.assertSame

@OptIn(TempContextStandInApi::class)
internal class SpanStorageTest {

    @Test
    fun testSpanStorage() {
        val span = FakeSpan()
        val root = internalRootContext()
        val newCtx = root.storeSpan(span)
        assertSame(span, newCtx.extractSpan())
    }

    @Test
    fun testStoringMultipleSpans() {
        val span = FakeSpan("a")
        val otherSpan = FakeSpan("b")
        val root = internalRootContext()
        val newCtx = root.storeSpan(span)

        val finalCtx = newCtx.storeSpan(otherSpan)
        assertSame(otherSpan, finalCtx.extractSpan())
    }
}
