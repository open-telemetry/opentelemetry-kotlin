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

    private val root: Context = FakeContext()
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
        val ctx1 = FakeContext()
        val ctx2 = FakeContext()
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
    fun testAttachingCurrentContextReportsButStillAttaches() {
        val ctx = FakeContext()
        val outer = storage.attach(ctx)
        val inner = storage.attach(ctx)

        assertEquals(2, delegate.attachCount)
        val error = assertIs<SdkError.ApiMisuse>(errors.single())
        assertEquals("Context.attach", error.api)
        assertEquals(SdkErrorSeverity.WARNING, error.severity)

        assertTrue(inner.detach())
        assertTrue(outer.detach())
        assertSame(root, storage.current())
        assertEquals(1, errors.size)
    }

    @Test
    fun testDoubleDetachReportsAndSkipsDelegate() {
        val scope = storage.attach(FakeContext())
        assertTrue(scope.detach())
        assertFalse(scope.detach())

        assertEquals(1, delegate.detachCount)
        val error = assertIs<SdkError.ApiMisuse>(errors.single())
        assertEquals("Scope.detach", error.api)
        assertEquals("Scope.detach() called on an already-detached scope", error.message)
        assertEquals(SdkErrorSeverity.WARNING, error.severity)
    }

    @Test
    fun testOutOfOrderDetachReportsButStillDetaches() {
        val scope1 = storage.attach(FakeContext())
        val ctx2 = FakeContext()
        val scope2 = storage.attach(ctx2)

        assertTrue(scope1.detach())
        assertEquals(1, delegate.detachCount)
        val error = assertIs<SdkError.ApiMisuse>(errors.single())
        assertEquals("Scope.detach", error.api)
        assertEquals("Scope.detach() called out of order — context has already changed", error.message)

        assertTrue(scope2.detach())
        assertEquals(2, delegate.detachCount)
    }

    @Test
    fun testDelegateDetachResultIsReturned() {
        delegate.detachResult = false
        assertFalse(storage.attach(FakeContext()).detach())
        assertTrue(errors.isEmpty())
    }

    @Test
    fun testThrowingCurrentFallsBackToRoot() {
        val storage = MisuseReportingCurrentContextStorage(ThrowingStorage(), { root }, errors::add)
        assertSame(root, storage.current())
        val error = assertIs<SdkError.UserCodeError>(errors.single())
        assertIs<IllegalStateException>(error.cause)
        assertEquals("CurrentContextStorage.current threw", error.message)
    }

    @Test
    fun testThrowingAttachReturnsFallbackScope() {
        val storage = MisuseReportingCurrentContextStorage(ThrowingStorage(), { root }, errors::add)
        val scope = storage.attach(FakeContext())

        assertSame(FallbackScope, scope)
        assertTrue(scope.detach())
        assertEquals(
            listOf("CurrentContextStorage.current threw", "CurrentContextStorage.attach threw"),
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

        assertFalse(storage.attach(FakeContext()).detach())
        val error = assertIs<SdkError.UserCodeError>(errors.single())
        assertEquals("Scope.detach threw", error.message)
    }

    @Test
    fun testPrecedenceStorageAttachLowWhileHighIsSet() {
        val layered = PrecedenceStorage(root)
        val storage = MisuseReportingCurrentContextStorage(layered, { root }, errors::add)
        val high = FakeContext()
        val low = FakeContext()

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
        val low = FakeContext()
        val high = FakeContext()

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
        val lowScope = storage.attach(FakeContext())
        layered.target = Layer.HIGH
        val high = FakeContext()
        val highScope = storage.attach(high)

        assertTrue(lowScope.detach())
        assertTrue(highScope.detach())
        assertSame(root, storage.current())
        val error = assertIs<SdkError.ApiMisuse>(errors.single())
        assertEquals("Scope.detach() called out of order — context has already changed", error.message)
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
