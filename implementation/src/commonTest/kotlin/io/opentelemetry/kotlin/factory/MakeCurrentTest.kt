package io.opentelemetry.kotlin.factory

import io.opentelemetry.kotlin.tracing.FakeSpan
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertSame

internal class MakeCurrentTest {

    private lateinit var contextFactory: ContextFactoryImpl
    private lateinit var spanFactory: SpanFactoryImpl

    @BeforeTest
    fun setUp() {
        contextFactory = ContextFactoryImpl()
        spanFactory = SpanFactoryImpl(SpanContextFactoryImpl(IdGeneratorImpl()), contextFactory.spanKey)
    }

    @Test
    fun testMakeCurrent() {
        val span = FakeSpan()
        val scope = contextFactory.makeCurrent(span)
        try {
            assertSame(span, spanFactory.fromContext(contextFactory.implicit()))
        } finally {
            scope.detach()
        }
    }

    @Test
    fun testMakeCurrentDetach() {
        val root = contextFactory.root()
        val span = FakeSpan()
        val scope = contextFactory.makeCurrent(span)
        scope.detach()
        assertSame(root, contextFactory.implicit())
    }

    @Test
    fun testNestedUnwind() {
        val span1 = FakeSpan("a")
        val span2 = FakeSpan("b")

        val scope1 = contextFactory.makeCurrent(span1)
        assertSame(span1, spanFactory.fromContext(contextFactory.implicit()))

        val scope2 = contextFactory.makeCurrent(span2)
        assertSame(span2, spanFactory.fromContext(contextFactory.implicit()))

        scope2.detach()
        assertSame(span1, spanFactory.fromContext(contextFactory.implicit()))

        scope1.detach()
        assertSame(contextFactory.root(), contextFactory.implicit())
    }
}
