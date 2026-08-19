package io.opentelemetry.kotlin.context

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.ThreadLocal
import kotlinx.coroutines.ExecutorCoroutineDispatcher
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame
import kotlin.test.assertTrue

@OptIn(ExperimentalApi::class)
internal class CoroutineContextElementTest {

    private lateinit var storage: TestStorage

    @BeforeTest
    fun setUp() {
        storage = TestStorage()
    }

    @Test
    fun testContextVisibleInsideCoroutine() = runBlocking {
        val ctx = TestContext("ctx", storage)
        withContext(ctx.asCoroutineContextElement()) {
            assertSame(ctx, storage.implicitContext())
        }
    }

    @Test
    fun testRootVisibleWhenNoElementPresent() = runBlocking {
        assertSame(storage.root, storage.implicitContext())
        launch { assertSame(storage.root, storage.implicitContext()) }.join()
    }

    @Test
    fun testContextSurvivesSuspension() = runTest {
        val ctx = TestContext("ctx", storage)
        withContext(ctx.asCoroutineContextElement()) {
            assertSame(ctx, storage.implicitContext())
            yield()
            assertSame(ctx, storage.implicitContext())
        }
    }

    @Test
    fun testThreadHops() {
        val ctx = TestContext("ctx", storage)
        val first = singleThreadDispatcher("otel-test-1")
        val second = singleThreadDispatcher("otel-test-2")
        try {
            runBlocking {
                val threads = mutableSetOf<String>()
                withContext(first + ctx.asCoroutineContextElement()) {
                    threads += Thread.currentThread().name
                    assertSame(ctx, storage.implicitContext())
                    withContext(second) {
                        threads += Thread.currentThread().name
                        assertSame(ctx, storage.implicitContext())
                    }
                    assertSame(ctx, storage.implicitContext())
                }
                assertEquals(2, threads.size)
            }
        } finally {
            first.close()
            second.close()
        }
    }

    @Test
    fun testChildCoroutinesInheritance() = runBlocking {
        val ctx = TestContext("ctx", storage)
        withContext(ctx.asCoroutineContextElement()) {
            launch { assertSame(ctx, storage.implicitContext()) }.join()
            val result = async { storage.implicitContext() }.await()
            assertSame(ctx, result)
        }
    }

    @Test
    fun testNestedContextRestoration() = runBlocking {
        val outer = TestContext("outer", storage)
        val inner = TestContext("inner", storage)
        withContext(outer.asCoroutineContextElement()) {
            assertSame(outer, storage.implicitContext())
            withContext(inner.asCoroutineContextElement()) {
                assertSame(inner, storage.implicitContext())
            }
            assertSame(outer, storage.implicitContext())
        }
        assertSame(storage.root, storage.implicitContext())
    }

    @Test
    fun testConcurrentCoroutinesIsolation() {
        val ctxA = TestContext("a", storage)
        val ctxB = TestContext("b", storage)

        val dispatcher = singleThreadDispatcher("otel-shared")
        dispatcher.use {
            runBlocking(dispatcher) {
                val a = launch(ctxA.asCoroutineContextElement()) {
                    repeat(5) {
                        assertSame(ctxA, storage.implicitContext())
                        yield()
                    }
                }
                val b = launch(ctxB.asCoroutineContextElement()) {
                    repeat(5) {
                        assertSame(ctxB, storage.implicitContext())
                        yield()
                    }
                }
                a.join()
                b.join()
            }
        }
    }

    @Test
    fun testCallingThreadUnaffected() {
        val ctx = TestContext("ctx", storage)
        val dispatcher = singleThreadDispatcher("otel-worker")
        dispatcher.use {
            runBlocking {
                assertSame(storage.root, storage.implicitContext())
                withContext(dispatcher + ctx.asCoroutineContextElement()) {
                    assertSame(ctx, storage.implicitContext())
                }
                assertSame(storage.root, storage.implicitContext())
            }
        }
    }

    @Test
    fun testRestorationAfterThrow() = runBlocking {
        val ctx = TestContext("ctx", storage)
        assertFailsWith<IllegalStateException> {
            withContext(ctx.asCoroutineContextElement()) {
                assertSame(ctx, storage.implicitContext())
                error("boom")
            }
        }
        assertSame(storage.root, storage.implicitContext())
    }

    @Test
    fun testScopeSuspension() = runBlocking {
        val ctx = TestContext("ctx", storage)
        withContext(ctx.asCoroutineContextElement()) {
            yield()
            yield()
        }
        assertTrue(ctx.attachCount.get() > 1, "expected repeated attaches, got ${ctx.attachCount.get()}")
        assertEquals(ctx.attachCount.get(), ctx.detachCount.get())
        assertSame(storage.root, storage.implicitContext())
    }

    private fun singleThreadDispatcher(name: String): ExecutorCoroutineDispatcher =
        Executors.newSingleThreadExecutor { runnable -> Thread(runnable, name) }.asCoroutineDispatcher()
}

@OptIn(ExperimentalApi::class)
private class TestStorage : ImplicitContextStorage {
    val root: Context = FakeContext()
    private val current = ThreadLocal<Context>()

    override fun implicitContext(): Context = current.get() ?: root

    override fun setImplicitContext(context: Context) {
        current.set(context)
    }
}

@OptIn(ExperimentalApi::class)
private class TestContext(
    private val name: String,
    private val storage: TestStorage,
    delegate: Context = FakeContext(),
) : Context by delegate {

    val attachCount = AtomicInteger()
    val detachCount = AtomicInteger()

    override fun attach(): Scope {
        attachCount.incrementAndGet()
        val previous = storage.implicitContext()
        storage.setImplicitContext(this)
        var detached = false
        return FakeScope {
            if (detached) {
                false
            } else {
                detached = true
                detachCount.incrementAndGet()
                storage.setImplicitContext(previous)
                true
            }
        }
    }
    override fun toString(): String = "TestContext($name)"
}
