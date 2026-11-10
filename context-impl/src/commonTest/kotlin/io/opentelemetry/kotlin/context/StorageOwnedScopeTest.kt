package io.opentelemetry.kotlin.context

import io.opentelemetry.kotlin.error.FakeSdkErrorHandler
import io.opentelemetry.kotlin.factory.ContextFactoryImpl
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertSame
import kotlin.test.assertTrue

internal class StorageOwnedScopeTest {

    private lateinit var handler: FakeSdkErrorHandler
    private lateinit var storage: FakeStackCurrentContextStorage
    private lateinit var factory: ContextFactoryImpl

    @BeforeTest
    fun setUp() {
        handler = FakeSdkErrorHandler()
        storage = FakeStackCurrentContextStorage()
        factory = ContextFactoryImpl(handler, storage)
    }

    @Test
    fun testAttachDelegatesToStorage() {
        val ctx = newContext()
        val scope = ctx.attach()
        assertSame(ctx, factory.currentContext())
        assertEquals(1, storage.attachCount)

        assertTrue(scope.detach())
        assertSame(factory.rootContext(), factory.currentContext())
        assertEquals(1, storage.restoreCount)
    }

    @Test
    fun testAttachCurrentContextDoesNotDelegate() {
        val ctx = newContext()
        val outer = ctx.attach()
        assertTrue(ctx.attach().detach())
        assertEquals(1, storage.attachCount)
        assertTrue(outer.detach())
    }

    @Test
    fun testDoubleDetachDoesNotRestoreTwice() {
        val scope = newContext().attach()
        assertTrue(scope.detach())
        assertFalse(scope.detach())
        assertEquals(1, storage.restoreCount)
        assertEquals(1, handler.apiMisuses.size)
    }

    @Test
    fun testOutOfOrderDetachDoesNotRestore() {
        val scope1 = newContext().attach()
        val scope2 = newContext().attach()
        assertFalse(scope1.detach())
        assertEquals(0, storage.restoreCount)
        assertEquals(1, handler.apiMisuses.size)

        assertTrue(scope2.detach())
        assertTrue(scope1.detach())
        assertSame(factory.rootContext(), factory.currentContext())
    }

    private fun newContext(): Context =
        factory.rootContext().set(createContextKey("key"), Any())

    @OptIn(TempContextStandInApi::class)
    private class FakeStackCurrentContextStorage : CurrentContextStorage {

        private val stack = ArrayDeque<Context>()

        var attachCount = 0
        var restoreCount = 0

        override fun attach(context: Context): Scope {
            attachCount++
            stack.addLast(context)
            return object : Scope {
                override fun detach(): Boolean {
                    restoreCount++
                    stack.removeLast()
                    return true
                }
            }
        }

        override fun current(): Context = stack.lastOrNull() ?: internalRootContext()
    }
}
