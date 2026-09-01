package io.opentelemetry.kotlin.context

import io.opentelemetry.kotlin.error.FakeSdkErrorHandler
import io.opentelemetry.kotlin.error.SdkErrorSeverity
import io.opentelemetry.kotlin.factory.ContextFactoryImpl
import io.opentelemetry.kotlin.factory.IdGeneratorImpl
import io.opentelemetry.kotlin.factory.SpanContextFactoryImpl
import io.opentelemetry.kotlin.factory.SpanFactoryImpl
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class ScopeImplTest {

    private lateinit var handler: FakeSdkErrorHandler
    private lateinit var factory: ContextFactoryImpl
    private lateinit var storage: DefaultImplicitContextStorage

    @BeforeTest
    fun setUp() {
        handler = FakeSdkErrorHandler()
        factory = ContextFactoryImpl(
            SpanFactoryImpl(SpanContextFactoryImpl(IdGeneratorImpl())),
            sdkErrorHandler = handler,
        )
        storage = DefaultImplicitContextStorage(factory::root)
    }

    @Test
    fun testCreateWithMatchingContextsReportsApiMisuse() {
        val root = factory.root()
        val scope = ScopeImpl.create(root, root, storage, handler)

        assertTrue(scope.detach())
        assertEquals(1, handler.apiMisuses.size)
        val error = handler.apiMisuses.single()
        assertEquals("Context.attach", error.api)
        assertEquals("Cannot create scope with two matching contexts", error.message)
        assertEquals(SdkErrorSeverity.WARNING, error.severity)
    }

    @Test
    fun testDoubleDetachReportsApiMisuse() {
        val newCtx = newContext()
        val scope = newCtx.attach()

        assertTrue(scope.detach())
        assertFalse(scope.detach())

        assertEquals(1, handler.apiMisuses.size)
        val error = handler.apiMisuses.single()
        assertEquals("Scope.detach", error.api)
        assertEquals("Scope.detach() called on an already-detached scope", error.message)
        assertEquals(SdkErrorSeverity.WARNING, error.severity)
    }

    @Test
    fun testOutOfOrderDetachReportsApiMisuse() {
        val ctx1 = newContext()
        val scope1 = ctx1.attach()
        val ctx2 = newContext()
        val scope2 = ctx2.attach()

        assertFalse(scope1.detach())
        assertTrue(scope2.detach())

        assertEquals(1, handler.apiMisuses.size)
        val error = handler.apiMisuses.single()
        assertEquals("Scope.detach", error.api)
        assertEquals("Scope.detach() called out of order — context has already changed", error.message)
        assertEquals(SdkErrorSeverity.WARNING, error.severity)
    }

    @Test
    fun testDetachSucceedsOnceContextIsRestored() {
        val ctx1 = newContext()
        val scope1 = ctx1.attach()
        val ctx2 = newContext()
        val scope2 = ctx2.attach()

        // scope1 can't detach while ctx2 is current
        assertFalse(scope1.detach())
        assertEquals(1, handler.apiMisuses.size)

        // once scope2 restores ctx1, scope1 is detachable again.
        assertTrue(scope2.detach())
        assertTrue(scope1.detach())
        assertEquals(1, handler.apiMisuses.size)
    }

    private fun newContext(): Context =
        factory.root().set(factory.createKey<String>("key"), "value")
}
