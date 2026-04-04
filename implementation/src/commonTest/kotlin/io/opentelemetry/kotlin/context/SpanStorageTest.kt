package io.opentelemetry.kotlin.context

import io.opentelemetry.kotlin.factory.ContextFactoryImpl
import io.opentelemetry.kotlin.factory.IdGeneratorImpl
import io.opentelemetry.kotlin.factory.SpanContextFactoryImpl
import io.opentelemetry.kotlin.factory.SpanFactoryImpl
import io.opentelemetry.kotlin.tracing.FakeSpan
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertSame

internal class SpanStorageTest {

    private val spanContextFactory = SpanContextFactoryImpl(IdGeneratorImpl())
    private val contextFactory = ContextFactoryImpl(spanContextFactory)
    private val spanFactory = SpanFactoryImpl(spanContextFactory, contextFactory.spanKey)

    @Test
    fun testSpanStorage() {
        val span = FakeSpan()
        val root = contextFactory.root()
        val newCtx = contextFactory.storeSpan(root, span)
        val retrievedSpan = spanFactory.fromContext(newCtx)
        assertSame(span, retrievedSpan)
    }

    @Test
    fun testStoringMultipleSpans() {
        val span = FakeSpan("a")
        val otherSpan = FakeSpan("b")
        val root = contextFactory.root()
        val newCtx = contextFactory.storeSpan(root, span)

        val finalCtx = contextFactory.storeSpan(newCtx, otherSpan)
        val retrievedSpan = spanFactory.fromContext(finalCtx)
        assertSame(otherSpan, retrievedSpan)
    }

    @Test
    fun testCurrentSpanReturnsInvalid() {
        assertFalse(contextFactory.currentSpan().spanContext.isValid)
        assertFalse(contextFactory.currentSpan().isRecording())
    }

    @Test
    fun testCurrentSpanReturnsActive() {
        val span = FakeSpan()
        val ctx = contextFactory.storeSpan(contextFactory.implicit(), span)
        val scope = ctx.attach()
        try {
            assertSame(span, contextFactory.currentSpan())
        } finally {
            scope.detach()
        }
    }
}
