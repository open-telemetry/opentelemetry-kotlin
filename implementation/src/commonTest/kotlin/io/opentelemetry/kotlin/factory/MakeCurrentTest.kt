package io.opentelemetry.kotlin.factory

import io.opentelemetry.kotlin.tracing.FakeSpan
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertSame

internal class MakeCurrentTest {

    private lateinit var contextFactory: ContextFactoryImpl

    @BeforeTest
    fun setUp() {
        contextFactory = ContextFactoryImpl()
    }

    @Test
    fun testMakeCurrent() {
        val span = FakeSpan()
        val scope = contextFactory.current().storeSpan(span).attach()
        try {
            assertSame(span, contextFactory.current().extractSpan())
        } finally {
            scope.detach()
        }
    }

    @Test
    fun testMakeCurrentDetach() {
        val root = contextFactory.root()
        val span = FakeSpan()
        val scope = contextFactory.current().storeSpan(span).attach()
        scope.detach()
        assertSame(root, contextFactory.current())
    }

    @Test
    fun testNestedUnwind() {
        val span1 = FakeSpan("a")
        val span2 = FakeSpan("b")

        val scope1 = contextFactory.current().storeSpan(span1).attach()
        assertSame(span1, contextFactory.current().extractSpan())

        val scope2 = contextFactory.current().storeSpan(span2).attach()
        assertSame(span2, contextFactory.current().extractSpan())

        scope2.detach()
        assertSame(span1, contextFactory.current().extractSpan())

        scope1.detach()
        assertSame(contextFactory.root(), contextFactory.current())
    }
}
