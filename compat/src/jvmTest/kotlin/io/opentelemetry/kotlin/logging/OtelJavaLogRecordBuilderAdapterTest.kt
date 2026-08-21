package io.opentelemetry.kotlin.logging

import io.opentelemetry.api.common.AttributeKey
import io.opentelemetry.kotlin.aliases.OtelJavaContext
import io.opentelemetry.kotlin.aliases.OtelJavaContextKey
import org.junit.Test
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertFalse

internal class OtelJavaLogRecordBuilderAdapterTest {

    @Test
    fun `test log record builder adapter`() {
        val impl = FakeLogger("logger")
        val adapter = OtelJavaLogRecordBuilderAdapter(impl)

        val now = Instant.now()
        adapter.setObservedTimestamp(now)
        adapter.setTimestamp(now)

        val key = OtelJavaContextKey.named<String>("key")
        val ctx = OtelJavaContext.root().with(key, "value")
        adapter.setContext(ctx)
        val body = "Hello, World!"
        adapter.setBody(body)
        adapter.emit()

        val log = impl.logs.single()
        assertEquals(body, log.body)

        val factor = 1000000
        val expected = now.toEpochMilli() * factor
        assertEquals(expected, (checkNotNull(log.timestamp) / factor) * factor)
        assertEquals(expected, (checkNotNull(log.observedTimestamp) / factor) * factor)
    }

    @Test
    fun `test attributes preserve their types`() {
        val impl = FakeLogger("logger")
        val adapter = OtelJavaLogRecordBuilderAdapter(impl)

        adapter.setAttribute(AttributeKey.stringKey("str"), "hello")
        adapter.setAttribute(AttributeKey.longKey("long"), 42L)
        adapter.setAttribute(AttributeKey.doubleKey("double"), 42.0)
        adapter.setAttribute(AttributeKey.booleanKey("bool"), true)
        adapter.setAttribute(AttributeKey.doubleArrayKey("doubleList"), listOf(1.0, 2.0))
        adapter.emit()

        val attrs = impl.logs.single().attributes
        assertEquals("hello", attrs["str"])
        assertEquals(42L, attrs["long"])
        // A whole-valued double must not collapse to a long, nor to the string "42.0".
        assertEquals(42.0, attrs["double"])
        assertEquals(true, attrs["bool"])
        assertEquals(listOf(1.0, 2.0), attrs["doubleList"])
    }

    @Test
    fun `test null attribute is dropped`() {
        val impl = FakeLogger("logger")
        val adapter = OtelJavaLogRecordBuilderAdapter(impl)

        // Must not throw: attrs is a ConcurrentHashMap, which forbids null values.
        adapter.setAttribute(AttributeKey.stringKey("nullable"), null)
        adapter.emit()

        assertFalse(impl.logs.single().attributes.containsKey("nullable"))
    }
}
