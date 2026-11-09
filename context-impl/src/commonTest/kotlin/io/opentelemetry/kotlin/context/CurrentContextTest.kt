package io.opentelemetry.kotlin.context

import io.opentelemetry.kotlin.error.NoopSdkErrorHandler
import io.opentelemetry.kotlin.factory.ContextFactory
import io.opentelemetry.kotlin.factory.ContextFactoryImpl
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertSame
import kotlin.test.assertTrue

internal class CurrentContextTest {

    private lateinit var factory: ContextFactory

    @BeforeTest
    fun setUp() {
        factory = ContextFactoryImpl()
    }

    @Test
    fun testSameContextsScopeIsAlreadyDetached() {
        val scope = ScopeImpl.create(
            factory.rootContext(),
            factory.rootContext(),
            SingleSlotCurrentContextStorage(factory::root),
            NoopSdkErrorHandler,
        ) {}
        assertTrue(scope.detach())
    }

    @Test
    fun testDupeAttach() {
        val newCtx = newContext()
        newCtx.attach()
        assertSame(newCtx, factory.currentContext())

        val next = newCtx.attach()
        assertSame(newCtx, factory.currentContext())

        next.detach()
        assertSame(newCtx, factory.currentContext())
    }

    @Test
    fun testDupeDetach() {
        assertSame(factory.rootContext(), factory.currentContext())

        val newCtx = newContext()
        val scope = newCtx.attach()
        assertSame(newCtx, factory.currentContext())

        scope.detach()
        assertSame(factory.rootContext(), factory.currentContext())

        scope.detach()
        assertSame(factory.rootContext(), factory.currentContext())
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
    fun testCurrentContext() {
        // assert default is root
        val root = factory.rootContext()
        assertSame(root, factory.currentContext())

        // set first scope
        val ctx1 = newContext()
        val scope1 = ctx1.attach()
        assertSame(ctx1, factory.currentContext())

        // set second scope
        val ctx2 = newContext()
        val scope2 = ctx2.attach()
        assertSame(ctx2, factory.currentContext())

        // invalid call as not current context, ignore.
        scope1.detach()
        assertSame(ctx2, factory.currentContext())

        // detach current context
        scope2.detach()
        assertSame(ctx1, factory.currentContext())

        // detach current context
        scope1.detach()
        assertSame(root, factory.currentContext())
    }

    private fun newContext(): Context =
        factory.rootContext().set(createContextKey<String>("key"), "value")
}
