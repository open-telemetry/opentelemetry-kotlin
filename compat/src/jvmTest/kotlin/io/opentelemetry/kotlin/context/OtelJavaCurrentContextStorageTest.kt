package io.opentelemetry.kotlin.context

import io.opentelemetry.kotlin.aliases.OtelJavaBaggage
import io.opentelemetry.kotlin.aliases.OtelJavaContext
import io.opentelemetry.kotlin.aliases.OtelJavaContextKey
import io.opentelemetry.kotlin.aliases.OtelJavaSpan
import io.opentelemetry.kotlin.aliases.OtelJavaSpanContext
import io.opentelemetry.kotlin.aliases.OtelJavaTraceFlags
import io.opentelemetry.kotlin.aliases.OtelJavaTraceState
import io.opentelemetry.kotlin.baggage.Baggage
import io.opentelemetry.kotlin.baggage.createBaggage
import io.opentelemetry.kotlin.error.FakeSdkErrorHandler
import io.opentelemetry.kotlin.error.setProcessSdkErrorHandler
import io.opentelemetry.kotlin.tracing.Span
import io.opentelemetry.kotlin.tracing.SpanContext
import io.opentelemetry.kotlin.tracing.createInvalidSpanContext
import io.opentelemetry.kotlin.tracing.createNonRecordingSpan
import io.opentelemetry.kotlin.tracing.createSpanContext
import io.opentelemetry.kotlin.tracing.model.OtelJavaSpanAdapter
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotSame
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

private const val TRACE_ID = "0af7651916cd43dd8448eb211c80319c"
private const val JAVA_SPAN_ID = "00f067aa0ba902b7"

internal class OtelJavaCurrentContextStorageTest {

    private val root = rootContext()
    private val storage = createOtelJavaCurrentContextStorage()
    private val key = createContextKey<String>("kotlin-key")
    private val javaKey = OtelJavaContextKey.named<String>("java-key")
    private val otherJavaKey = OtelJavaContextKey.named<String>("other-java-key")
    private val kotlinSpan = span("b7ad6b7169203331")
    private val javaSpan = OtelJavaSpan.wrap(
        OtelJavaSpanContext.create(TRACE_ID, JAVA_SPAN_ID, OtelJavaTraceFlags.getSampled(), OtelJavaTraceState.getDefault())
    )
    private val alice = createBaggage { put("user", "alice") }
    private val errors = FakeSdkErrorHandler()

    @Before
    fun setUp() {
        setProcessSdkErrorHandler(null)
        setProcessSdkErrorHandler(errors)
    }

    @After
    fun tearDown() {
        try {
            assertEquals(OtelJavaContext.root(), OtelJavaContext.current(), "Test leaked a scope")
        } finally {
            OtelJavaContext.root().makeCurrent()
            setProcessSdkErrorHandler(null)
        }
    }

    @Test
    fun `kotlin attach is visible to java and nested scopes restore in order`() {
        val outerSpan = span("c7ad6b7169203331")
        val outer = root.set(key, "outer").storeSpan(outerSpan).storeBaggage(alice)
        val inner = root.set(key, "inner").storeSpan(kotlinSpan)

        storage.attach(outer).use {
            storage.attach(inner).use {
                assertSame(inner, storage.current())
                assertEquals(kotlinSpan.spanContext.spanId, OtelJavaSpan.current().spanContext.spanId)
                assertTrue(OtelJavaBaggage.current().isEmpty)
            }
            assertSame(outer, storage.current())
            assertEquals(outerSpan.spanContext.spanId, OtelJavaSpan.current().spanContext.spanId)
            assertEquals("alice", OtelJavaBaggage.current().getEntryValue("user"))
            storage.attach(root).use {
                assertSame(OtelJavaContext.root(), OtelJavaContext.current())
                assertSame(root, storage.current())
            }
        }
        assertSame(root, storage.current())
    }

    @Test
    fun `java span over a kotlin context is overlaid and kept until kotlin changes it`() {
        storage.attach(root.set(key, "value").storeSpan(kotlinSpan)).use {
            OtelJavaContext.current().with(javaKey, "java").with(javaSpan).makeCurrent().use {
                val current = storage.current()
                assertEquals(JAVA_SPAN_ID, current.extractSpan().spanContext.spanId)
                assertEquals("value", current.get(key))
                assertSame(current, storage.current())

                // unrelated Kotlin change keeps Java's span instance and Java-only keys
                storage.attach(current.set(key, "changed")).use {
                    assertSame(javaSpan, OtelJavaSpan.current())
                    assertEquals("java", OtelJavaContext.current().get(javaKey))
                    assertEquals("changed", storage.current().get(key))
                }
                // a different wrapper with the same IDs replaces it (identity, not ID matching)
                storage.attach(current.storeSpan(createNonRecordingSpan(javaSpan.spanContext.toKotlin()))).use {
                    assertNotSame(javaSpan, OtelJavaSpan.current())
                    assertEquals(JAVA_SPAN_ID, OtelJavaSpan.current().spanContext.spanId)
                }
                storage.attach(current.storeSpan(kotlinSpan).storeBaggage(alice)).use {
                    assertEquals(kotlinSpan.spanContext.spanId, OtelJavaSpan.current().spanContext.spanId)
                    assertEquals("alice", OtelJavaBaggage.current().getEntryValue("user"))
                    assertEquals("java", OtelJavaContext.current().get(javaKey))
                }
                storage.attach(current.storeSpan(createNonRecordingSpan(createInvalidSpanContext()))).use {
                    assertFalse(OtelJavaSpan.current().spanContext.isValid)
                }
            }
            val wrapped = span("d7ad6b7169203331")
            OtelJavaContext.current().with(OtelJavaSpanAdapter(wrapped)).makeCurrent().use {
                assertSame(wrapped, storage.current().extractSpan())
            }
            OtelJavaContext.current().with(OtelJavaSpan.getInvalid()).makeCurrent().use {
                assertFalse(storage.current().extractSpan().spanContext.isValid)
                storage.attach(storage.current().set(key, "changed")).use {
                    assertFalse(OtelJavaSpan.current().spanContext.isValid)
                }
            }
        }
    }

    @Test
    fun `java baggage over a kotlin context is overlaid and cleared in both directions`() {
        storage.attach(root.set(key, "value").storeBaggage(alice)).use {
            OtelJavaBaggage.builder().put("x", "y").build().makeCurrent().use {
                val current = storage.current()
                assertEquals("y", current.extractBaggage().getValue("x"))
                assertNull(current.extractBaggage().getValue("user"))
                assertEquals("value", current.get(key))
                assertSame(current, storage.current())

                storage.attach(current.clearBaggage()).use {
                    assertTrue(OtelJavaBaggage.current().isEmpty)
                }
            }
            OtelJavaBaggage.empty().makeCurrent().use {
                assertTrue(storage.current().extractBaggage().asMap().isEmpty())
                storage.attach(storage.current().set(key, "changed")).use {
                    assertTrue(OtelJavaBaggage.current().isEmpty)
                }
            }
        }
    }

    @Test
    fun `java keys come from the context's own java origin, never java's current context`() {
        val captured = storage.attach(root.set(key, "value")).use {
            OtelJavaContext.current().with(javaKey, "A").makeCurrent().use { storage.current() }
        }
        OtelJavaContext.current().with(otherJavaKey, "B").makeCurrent().use {
            storage.attach(captured.set(key, "later")).use {
                assertEquals("A", OtelJavaContext.current().get(javaKey))
                assertNull(OtelJavaContext.current().get(otherJavaKey))
            }
            storage.attach(root.set(key, "value")).use {
                assertNull(OtelJavaContext.current().get(otherJavaKey))
            }
        }
    }

    @Test
    fun `foreign java contexts keep java keys and are identity-stable`() {
        OtelJavaContext.root().with(javaKey, "java").with(javaSpan).makeCurrent().use {
            val current = storage.current()
            assertEquals(JAVA_SPAN_ID, current.extractSpan().spanContext.spanId)
            assertSame(current, storage.current())
            assertSame(current, OtelJavaContext.current().toOtelKotlinContext())
            assertSame(OtelJavaContext.current(), current.toOtelJavaContext())
            storage.attach(current.set(key, "value")).use {
                assertEquals("java", OtelJavaContext.current().get(javaKey))
                assertEquals("value", storage.current().get(key))
            }
        }
    }

    @Test
    fun `public helpers round trip onto the java origin`() {
        storage.attach(root.set(key, "value")).use {
            OtelJavaContext.current().with(javaKey, "java").with(javaSpan).makeCurrent().use {
                assertEquals("value", OtelJavaContext.current().toOtelKotlinContext().get(key))
                assertSame(OtelJavaContext.current(), storage.current().toOtelJavaContext())

                val java = storage.current().set(key, "changed").toOtelJavaContext()
                assertEquals("java", java.get(javaKey))
                assertSame(javaSpan, OtelJavaSpan.fromContext(java))
                assertEquals("changed", java.toOtelKotlinContext().get(key))
            }
        }
    }

    @Test
    fun `failing user code falls back and is reported once`() {
        val failsOnAttach = MapContext(failOn = setOf("extractSpan"))
        storage.attach(failsOnAttach).use {
            assertIs<OtelJavaContextAdapter>(OtelJavaContext.current())
            assertSame(failsOnAttach, storage.current())
        }
        assertIs<OtelJavaContextAdapter>(MapContext(failOn = setOf("get")).toOtelJavaContext())
        assertEquals(2, errors.userCodeErrors.size)

        storage.attach(MapContext(failOn = setOf("storeSpan")).set(key, "value")).use {
            OtelJavaContext.current().with(javaSpan).makeCurrent().use {
                val current = storage.current()
                assertNull(current.get(key))
                assertSame(current, storage.current())
                assertEquals(JAVA_SPAN_ID, current.extractSpan().spanContext.spanId)
            }
        }
        assertEquals(3, errors.userCodeErrors.size)
    }

    private fun span(spanId: String) = createNonRecordingSpan(createSpanContext(TRACE_ID, spanId))

    private fun OtelJavaSpanContext.toKotlin() = createSpanContext(traceId, spanId)

    private inline fun <T> Scope.use(block: () -> T): T = try {
        block()
    } finally {
        detach()
    }

    /**
     * A Kotlin context that isn't backed by Java, like the SDK's. Methods named in [failOn] throw.
     */
    private class MapContext(
        private val values: Map<ContextKey<*>, Any?> = emptyMap(),
        private val failOn: Set<String> = emptySet(),
    ) : Context {
        private fun fail(method: String) = check(method !in failOn) { "$method failed" }

        override fun <T> set(key: ContextKey<T>, value: T?): Context = MapContext(values + (key to value), failOn)

        @Suppress("UNCHECKED_CAST")
        override fun <T> get(key: ContextKey<T>): T? = fail("get").let { values[key] as T? }
        override fun attach(): Scope = error("Not used")
        override fun storeSpan(span: Span): Context = fail("storeSpan").let { set(SPAN, span) }
        override fun storeSpanContext(spanContext: SpanContext): Context =
            storeSpan(createNonRecordingSpan(spanContext))
        override fun extractSpan(): Span = fail("extractSpan").let { values[SPAN] as Span? ?: INVALID_SPAN }
        override fun storeBaggage(baggage: Baggage): Context = set(BAGGAGE, baggage)
        override fun extractBaggage(): Baggage = values[BAGGAGE] as Baggage? ?: EMPTY_BAGGAGE
        override fun clearBaggage(): Context = set(BAGGAGE, null)

        private companion object {
            val SPAN = createContextKey<Span>("span")
            val BAGGAGE = createContextKey<Baggage>("baggage")
            val INVALID_SPAN = createNonRecordingSpan(createInvalidSpanContext())
            val EMPTY_BAGGAGE = createBaggage()
        }
    }
}
