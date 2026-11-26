package io.opentelemetry.kotlin.context

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.error.SdkError
import io.opentelemetry.kotlin.error.SdkErrorSeverity
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertSame
import kotlin.test.assertTrue

@OptIn(ExperimentalApi::class)
internal class MisuseReportingCurrentContextStorageTest {

    private val root: Context = ContextValue(NoStorage)
    private lateinit var errors: MutableList<SdkError>
    private lateinit var delegate: StackStorage
    private lateinit var storage: MisuseReportingCurrentContextStorage

    @BeforeTest
    fun setUp() {
        errors = mutableListOf()
        delegate = StackStorage(root)
        storage = MisuseReportingCurrentContextStorage(delegate, { root }, errors::add)
    }

    @Test
    fun testInOrderAttachDetachReportsNothing() {
        val ctx1 = ContextValue(NoStorage)
        val ctx2 = ContextValue(NoStorage)
        val scope1 = storage.attach(ctx1)
        val scope2 = storage.attach(ctx2)
        assertSame(ctx2, storage.current())

        assertTrue(scope2.detach())
        assertSame(ctx1, storage.current())
        assertTrue(scope1.detach())
        assertSame(root, storage.current())
        assertTrue(errors.isEmpty())
    }

    @Test
    fun testAttachingCurrentContextReportsNothing() {
        val ctx = ContextValue(NoStorage)
        val outer = storage.attach(ctx)
        val inner = storage.attach(ctx)

        assertEquals(2, delegate.attachCount)
        assertTrue(inner.detach())
        assertTrue(outer.detach())
        assertSame(root, storage.current())
        assertTrue(errors.isEmpty())
    }

    @Test
    fun testDoubleDetachReportsAndSkipsDelegate() {
        val scope = storage.attach(ContextValue(NoStorage))
        assertTrue(scope.detach())
        assertFalse(scope.detach())

        assertEquals(1, delegate.detachCount)
        val error = assertIs<SdkError.ApiMisuse>(errors.single())
        assertEquals("Scope.detach", error.api)
        assertEquals(ALREADY_DETACHED_MSG, error.message)
        assertEquals(SdkErrorSeverity.WARNING, error.severity)
    }

    @Test
    fun testOutOfOrderDetachReportsButStillDetaches() {
        val scope1 = storage.attach(ContextValue(NoStorage))
        val ctx2 = ContextValue(NoStorage)
        val scope2 = storage.attach(ctx2)

        assertTrue(scope1.detach())
        assertEquals(1, delegate.detachCount)
        val error = assertIs<SdkError.ApiMisuse>(errors.single())
        assertEquals("Scope.detach", error.api)
        assertEquals(OUT_OF_ORDER_MSG, error.message)

        assertTrue(scope2.detach())
        assertEquals(2, delegate.detachCount)
    }

    @Test
    fun testRepeatedDoubleDetachReportsOnce() {
        repeat(1000) {
            val scope = storage.attach(ContextValue(NoStorage))
            assertTrue(scope.detach())
            assertFalse(scope.detach())
        }
        assertEquals(1000, delegate.detachCount)
        assertEquals(listOf(ALREADY_DETACHED_MSG), errors.map { it.message })
    }

    @Test
    fun testRepeatedOutOfOrderDetachReportsOnce() {
        repeat(1000) {
            val scope1 = storage.attach(ContextValue(NoStorage))
            val scope2 = storage.attach(ContextValue(NoStorage))
            assertTrue(scope1.detach())
            assertTrue(scope2.detach())
        }
        assertEquals(2000, delegate.detachCount)
        assertEquals(listOf(OUT_OF_ORDER_MSG), errors.map { it.message })
    }

    @Test
    fun testMisuseLatchesAreIndependent() {
        val scope1 = storage.attach(ContextValue(NoStorage))
        storage.attach(ContextValue(NoStorage))
        assertTrue(scope1.detach())
        assertFalse(scope1.detach())
        assertEquals(listOf(OUT_OF_ORDER_MSG, ALREADY_DETACHED_MSG), errors.map { it.message })
    }

    @Test
    fun testDelegateDetachResultIsReturned() {
        delegate.detachResult = false
        assertFalse(storage.attach(ContextValue(NoStorage)).detach())
        assertTrue(errors.isEmpty())
    }

    @Test
    fun testThrowingCurrentFallsBackToRoot() {
        val storage = MisuseReportingCurrentContextStorage(ThrowingStorage(), { root }, errors::add)
        assertSame(root, storage.current())
        val error = assertIs<SdkError.UserCodeError>(errors.single())
        assertIs<IllegalStateException>(error.cause)
        assertEquals("CurrentContextStorage.current threw. Later failures are not reported.", error.message)
    }

    @Test
    fun testRepeatedThrowingCurrentReportsOnce() {
        val storage = MisuseReportingCurrentContextStorage(ThrowingStorage(), { root }, errors::add)
        repeat(1000) { assertSame(root, storage.current()) }
        assertEquals(1, errors.size)
    }

    @Test
    fun testRepeatedThrowingAttachReportsOncePerMember() {
        val storage = MisuseReportingCurrentContextStorage(ThrowingStorage(), { root }, errors::add)
        repeat(1000) { assertSame(FallbackScope, storage.attach(ContextValue(NoStorage))) }
        assertEquals(
            listOf("CurrentContextStorage.attach threw. Later failures are not reported."),
            errors.map { it.message },
        )
    }

    @Test
    fun testThrowingAttachReturnsFallbackScope() {
        val storage = MisuseReportingCurrentContextStorage(ThrowingStorage(), { root }, errors::add)
        val scope = storage.attach(ContextValue(NoStorage))

        assertSame(FallbackScope, scope)
        assertTrue(scope.detach())
        assertEquals(
            listOf("CurrentContextStorage.attach threw. Later failures are not reported."),
            errors.map { it.message },
        )
        errors.forEach { assertIs<SdkError.UserCodeError>(it) }
    }

    @Test
    fun testThrowingDetachIsReported() {
        val throwingScopeStorage = object : CurrentContextStorage {
            override fun current(): Context = root
            override fun attach(context: Context): Scope = object : Scope {
                override fun detach(): Boolean = error("detach")
            }
        }
        val storage = MisuseReportingCurrentContextStorage(throwingScopeStorage, { root }, errors::add)

        assertFalse(storage.attach(ContextValue(NoStorage)).detach())
        val error = assertIs<SdkError.UserCodeError>(errors.single())
        assertEquals("Scope.detach threw. Later failures are not reported.", error.message)

        repeat(1000) { assertFalse(storage.attach(ContextValue(NoStorage)).detach()) }
        assertEquals(1, errors.size, "detach failures are latched across scopes")
    }

    @Test
    fun testPrecedenceStorageAttachLowWhileHighIsSet() {
        val layered = PrecedenceStorage(root)
        val storage = MisuseReportingCurrentContextStorage(layered, { root }, errors::add)
        val high = ContextValue(NoStorage)
        val low = ContextValue(NoStorage)

        layered.target = Layer.HIGH
        val highScope = storage.attach(high)
        layered.target = Layer.LOW
        val lowScope = storage.attach(low)
        assertSame(high, storage.current())
        assertSame(low, layered.low)

        assertTrue(lowScope.detach())
        assertSame(null, layered.low)
        assertSame(high, storage.current())
        assertTrue(highScope.detach())
        assertSame(root, storage.current())
        assertTrue(errors.isEmpty())
    }

    @Test
    fun testPrecedenceStorageInOrderNestedDetachReportsNothing() {
        val layered = PrecedenceStorage(root)
        val storage = MisuseReportingCurrentContextStorage(layered, { root }, errors::add)
        val low = ContextValue(NoStorage)
        val high = ContextValue(NoStorage)

        layered.target = Layer.LOW
        val lowScope = storage.attach(low)
        layered.target = Layer.HIGH
        val highScope = storage.attach(high)

        assertTrue(highScope.detach())
        assertSame(low, storage.current())
        assertTrue(lowScope.detach())
        assertSame(root, storage.current())
        assertTrue(errors.isEmpty())
    }

    @Test
    fun testPrecedenceStorageOutOfOrderDetachReportsOnce() {
        val layered = PrecedenceStorage(root)
        val storage = MisuseReportingCurrentContextStorage(layered, { root }, errors::add)

        layered.target = Layer.LOW
        val lowScope = storage.attach(ContextValue(NoStorage))
        layered.target = Layer.HIGH
        val high = ContextValue(NoStorage)
        val highScope = storage.attach(high)

        assertTrue(lowScope.detach())
        assertTrue(highScope.detach())
        assertSame(root, storage.current())
        val error = assertIs<SdkError.ApiMisuse>(errors.single())
        assertEquals(OUT_OF_ORDER_MSG, error.message)
    }

    private companion object {
        const val ALREADY_DETACHED_MSG =
            "Scope.detach() called on an already-detached scope. Later occurrences are not reported."
        const val OUT_OF_ORDER_MSG =
            "Scope.detach() called out of order — context has already changed. Later occurrences are not reported."
    }

    private object NoStorage : CurrentContextStorageSource {
        override fun installed(): CurrentContextStorage? = null
    }

    private class StackStorage(root: Context) : CurrentContextStorage {
        private var current: Context = root
        var attachCount = 0
        var detachCount = 0
        var detachResult = true

        override fun current(): Context = current

        override fun attach(context: Context): Scope {
            attachCount++
            val previous = current
            current = context
            return object : Scope {
                override fun detach(): Boolean {
                    detachCount++
                    current = previous
                    return detachResult
                }
            }
        }
    }

    private class ThrowingStorage : CurrentContextStorage {
        override fun current(): Context = error("current")
        override fun attach(context: Context): Scope = error("attach")
    }

    private enum class Layer { HIGH, LOW }

    private class PrecedenceStorage(private val root: Context) : CurrentContextStorage {
        var high: Context? = null
        var low: Context? = null
        var target: Layer = Layer.LOW

        override fun current(): Context = high ?: low ?: root

        override fun attach(context: Context): Scope = when (target) {
            Layer.HIGH -> {
                val previous = high
                high = context
                RestoreScope { high = previous }
            }
            Layer.LOW -> {
                val previous = low
                low = context
                RestoreScope { low = previous }
            }
        }
    }

    private class RestoreScope(private val restore: () -> Unit) : Scope {
        override fun detach(): Boolean {
            restore()
            return true
        }
    }
}
