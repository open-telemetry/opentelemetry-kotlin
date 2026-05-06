package io.opentelemetry.kotlin.context

import io.opentelemetry.kotlin.factory.ContextFactoryImpl
import io.opentelemetry.kotlin.factory.IdGeneratorImpl
import io.opentelemetry.kotlin.factory.SpanContextFactoryImpl
import io.opentelemetry.kotlin.factory.SpanFactoryImpl
import io.opentelemetry.kotlin.tracing.FakeSpan
import kotlin.test.Test
import kotlin.test.assertSame

internal class SpanStorageTest {

    private val spanFactory = SpanFactoryImpl(SpanContextFactoryImpl(IdGeneratorImpl()))
    private val contextFactory = ContextFactoryImpl(spanFactory)

    @Test
    fun testSpanStorage() {
        val span = FakeSpan()
        val root = contextFactory.root()
        val newCtx = root.storeSpan(span)
        assertSame(span, newCtx.extractSpan())
    }

    @Test
    fun testStoringMultipleSpans() {
        val span = FakeSpan("a")
        val otherSpan = FakeSpan("b")
        val root = contextFactory.root()
        val newCtx = root.storeSpan(span)

        val finalCtx = newCtx.storeSpan(otherSpan)
        assertSame(otherSpan, finalCtx.extractSpan())
    }
}
