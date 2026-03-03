package io.opentelemetry.kotlin.factory

import io.opentelemetry.kotlin.tracing.FakeSpanContext
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

internal class SpanFactoryImplTest {

    private val contextFactory = ContextFactoryImpl()
    private val factory = SpanFactoryImpl(SpanContextFactoryImpl(IdGeneratorImpl()), contextFactory.spanKey)

    @Test
    fun testInvalidSpan() {
        assertFalse(factory.invalid.spanContext.isValid)
    }

    @Test
    fun testFromSpanContext() {
        val spanContext = FakeSpanContext.VALID
        assertEquals(spanContext, factory.fromSpanContext(spanContext).spanContext)
    }
}
