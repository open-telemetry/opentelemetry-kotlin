package io.opentelemetry.kotlin.context

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.factory.ContextFactory
import io.opentelemetry.kotlin.factory.ContextFactoryImpl
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertSame

@OptIn(ExperimentalApi::class)
internal class ImplicitContextTest {

    private lateinit var factory: ContextFactory

    @BeforeTest
    fun setUp() {
        factory = ContextFactoryImpl()
    }

    @Test
    fun testSameContexts() {
        assertFailsWith(IllegalStateException::class) {
            ScopeImpl(
                factory.root(),
                factory.root(),
                DefaultImplicitContextStorage(factory::root)
            )
        }
    }

    @Test
    fun testDupeAttach() {
        val newCtx = factory.root().with(mapOf("key" to "value"))
        newCtx.attach()
        assertSame(newCtx, factory.implicitContext())

        val next = newCtx.attach()
        assertSame(newCtx, factory.implicitContext())

        next.detach()
        assertSame(newCtx, factory.implicitContext())
    }

    @Test
    fun testDupeDetach() {
        assertSame(factory.root(), factory.implicitContext())

        val newCtx = factory.root().with(mapOf("key" to "value"))
        val scope = newCtx.attach()
        assertSame(newCtx, factory.implicitContext())

        scope.detach()
        assertSame(factory.root(), factory.implicitContext())

        scope.detach()
        assertSame(factory.root(), factory.implicitContext())
    }

    @Test
    fun testImplicitContext() {
        // assert default is root
        val root = factory.root()
        assertSame(root, factory.implicitContext())

        // set first scope
        val ctx1 = root.with(mapOf("key" to "value"))
        val scope1 = ctx1.attach()
        assertSame(ctx1, factory.implicitContext())

        // set second scope
        val ctx2 = root.with(mapOf("another" to "value"))
        val scope2 = ctx2.attach()
        assertSame(ctx2, factory.implicitContext())

        // invalid call as not current implicit context, ignore.
        scope1.detach()
        assertSame(ctx2, factory.implicitContext())

        // detach current implicit context
        scope2.detach()
        assertSame(ctx1, factory.implicitContext())

        // detach current implicit context
        scope1.detach()
        assertSame(root, factory.implicitContext())
    }
}
