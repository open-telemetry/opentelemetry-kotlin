package io.opentelemetry.kotlin.context

import io.opentelemetry.kotlin.error.NoopSdkErrorHandler
import io.opentelemetry.kotlin.factory.ContextFactory
import io.opentelemetry.kotlin.factory.ContextFactoryImpl
import io.opentelemetry.kotlin.factory.IdGeneratorImpl
import io.opentelemetry.kotlin.factory.SpanContextFactoryImpl
import io.opentelemetry.kotlin.factory.SpanFactoryImpl
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertSame
import kotlin.test.assertTrue

internal class ImplicitContextTest {

    private lateinit var factory: ContextFactory

    @BeforeTest
    fun setUp() {
        factory = ContextFactoryImpl(SpanFactoryImpl(SpanContextFactoryImpl(IdGeneratorImpl())))
    }

    @Test
    fun testSameContextsScopeIsAlreadyDetached() {
        val scope = ScopeImpl.create(
            factory.root(),
            factory.root(),
            DefaultImplicitContextStorage(factory::root),
            NoopSdkErrorHandler,
        )
        assertTrue(scope.detach())
    }

    @Test
    fun testDupeAttach() {
        val newCtx = newContext()
        newCtx.attach()
        assertSame(newCtx, factory.implicit())

        val next = newCtx.attach()
        assertSame(newCtx, factory.implicit())

        next.detach()
        assertSame(newCtx, factory.implicit())
    }

    @Test
    fun testDupeDetach() {
        assertSame(factory.root(), factory.implicit())

        val newCtx = newContext()
        val scope = newCtx.attach()
        assertSame(newCtx, factory.implicit())

        scope.detach()
        assertSame(factory.root(), factory.implicit())

        scope.detach()
        assertSame(factory.root(), factory.implicit())
    }

    @Test
    fun testDetachReturnsTrueOnSuccess() {
        val newCtx = newContext()
        val scope = newCtx.attach()
        assertTrue(scope.detach())
    }

    @Test
    fun testDetachReturnsFalseWhenAlreadyDetached() {
        val newCtx = newContext()
        val scope = newCtx.attach()
        assertTrue(scope.detach())
        assertFalse(scope.detach())
    }

    @Test
    fun testDetachReturnsFalseWhenOutOfOrder() {
        val ctx1 = newContext()
        val scope1 = ctx1.attach()
        val ctx2 = newContext()
        val scope2 = ctx2.attach()

        // scope1 is out of order — ctx2 is current
        assertFalse(scope1.detach())

        // scope2 is still detachable
        assertTrue(scope2.detach())
    }

    @Test
    fun testImplicitContext() {
        // assert default is root
        val root = factory.root()
        assertSame(root, factory.implicit())

        // set first scope
        val ctx1 = newContext()
        val scope1 = ctx1.attach()
        assertSame(ctx1, factory.implicit())

        // set second scope
        val ctx2 = newContext()
        val scope2 = ctx2.attach()
        assertSame(ctx2, factory.implicit())

        // invalid call as not current implicit context, ignore.
        scope1.detach()
        assertSame(ctx2, factory.implicit())

        // detach current implicit context
        scope2.detach()
        assertSame(ctx1, factory.implicit())

        // detach current implicit context
        scope1.detach()
        assertSame(root, factory.implicit())
    }

    private fun newContext(): Context =
        factory.root().set(factory.createKey<String>("key"), "value")
}
