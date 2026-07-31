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
        val newCtx = factory.with(factory.root(), mapOf("key" to "value"))
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
        val ctx1 = factory.with(factory.root(), mapOf("key" to "value"))
        val scope1 = ctx1.attach()
        val ctx2 = factory.with(factory.root(), mapOf("another" to "value"))
        val scope2 = ctx2.attach()

        assertFalse(scope1.detach())
        assertTrue(scope2.detach())

        assertEquals(1, handler.apiMisuses.size)
        val error = handler.apiMisuses.single()
        assertEquals("Scope.detach", error.api)
        assertEquals("Scope.detach() called out of order — context has already changed", error.message)
        assertEquals(SdkErrorSeverity.WARNING, error.severity)
    }
}
