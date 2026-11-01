package io.opentelemetry.kotlin.context

import io.opentelemetry.kotlin.error.FakeSdkErrorHandler
import io.opentelemetry.kotlin.factory.ContextFactoryImpl
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertSame
import kotlin.test.assertTrue

internal class ScopedImplicitContextStorageTest {

    private lateinit var handler: FakeSdkErrorHandler
    private lateinit var storage: FakeScopedImplicitContextStorage
    private lateinit var factory: ContextFactoryImpl

    @BeforeTest
    fun setUp() {
        handler = FakeSdkErrorHandler()
        factory = ContextFactoryImpl(handler) { rootSupplier ->
            FakeScopedImplicitContextStorage(rootSupplier).also { storage = it }
        }
    }

    @Test
    fun testAttachDelegatesToStorage() {
        val ctx = newContext()
        val scope = ctx.attach()
        assertSame(ctx, factory.current())
        assertEquals(1, storage.attachCount)
        assertEquals(0, storage.setCount)

        assertTrue(scope.detach())
        assertSame(factory.root(), factory.current())
        assertEquals(0, storage.setCount)
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
        assertSame(factory.root(), factory.current())
    }

    private fun newContext(): Context =
        factory.root().set(createContextKey("key"), Any())

    private class FakeScopedImplicitContextStorage(
        rootSupplier: () -> Context,
    ) : ScopedImplicitContextStorage {

        private val root by lazy { rootSupplier() }
        private val stack = ArrayDeque<Context>()

        var attachCount = 0
        var setCount = 0
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

        override fun setImplicitContext(context: Context) {
            setCount++
        }

        override fun implicitContext(): Context = stack.lastOrNull() ?: root
    }
}
