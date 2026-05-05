package io.opentelemetry.kotlin.factory

import io.opentelemetry.kotlin.tracing.FakeSpan
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertSame

internal class MakeCurrentTest {

    private lateinit var contextFactory: ContextFactoryImpl

    @BeforeTest
    fun setUp() {
        val spanFactory = SpanFactoryImpl(SpanContextFactoryImpl(IdGeneratorImpl()))
        contextFactory = ContextFactoryImpl(spanFactory)
    }

    @Test
    fun testMakeCurrent() {
        val span = FakeSpan()
        val scope = contextFactory.implicit().storeSpan(span).attach()
        try {
            assertSame(span, contextFactory.implicit().extractSpan())
        } finally {
            scope.detach()
        }
    }

    @Test
    fun testMakeCurrentDetach() {
        val root = contextFactory.root()
        val span = FakeSpan()
        val scope = contextFactory.implicit().storeSpan(span).attach()
        scope.detach()
        assertSame(root, contextFactory.implicit())
    }

    @Test
    fun testNestedUnwind() {
        val span1 = FakeSpan("a")
        val span2 = FakeSpan("b")

        val scope1 = contextFactory.implicit().storeSpan(span1).attach()
        assertSame(span1, contextFactory.implicit().extractSpan())

        val scope2 = contextFactory.implicit().storeSpan(span2).attach()
        assertSame(span2, contextFactory.implicit().extractSpan())

        scope2.detach()
        assertSame(span1, contextFactory.implicit().extractSpan())

        scope1.detach()
        assertSame(contextFactory.root(), contextFactory.implicit())
    }
}
