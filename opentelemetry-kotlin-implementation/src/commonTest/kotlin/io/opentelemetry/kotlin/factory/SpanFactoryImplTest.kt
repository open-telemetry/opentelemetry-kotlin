package io.opentelemetry.kotlin.factory

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.tracing.FakeSpanContext
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

@OptIn(ExperimentalApi::class)
internal class SpanFactoryImplTest {

    private val sdkFactory = createSdkFactory()
    private val factory = sdkFactory.spanFactory

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
