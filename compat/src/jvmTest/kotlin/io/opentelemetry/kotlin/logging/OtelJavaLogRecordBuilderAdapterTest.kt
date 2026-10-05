package io.opentelemetry.kotlin.logging

import io.opentelemetry.api.common.AttributeKey
import io.opentelemetry.api.common.Value
import io.opentelemetry.kotlin.aliases.OtelJavaContext
import io.opentelemetry.kotlin.aliases.OtelJavaContextKey
import io.opentelemetry.kotlin.attributes.AnyValue
import org.junit.Test
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertSame

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
    fun `test event name is forwarded`() {
        val impl = FakeLogger("logger")
        val adapter = OtelJavaLogRecordBuilderAdapter(impl)

        adapter.setEventName("my.event")
        adapter.emit()

        assertEquals("my.event", impl.logs.single().eventName)
    }

    @Test
    fun `test exception is forwarded`() {
        val impl = RecordingLogger()
        val adapter = OtelJavaLogRecordBuilderAdapter(impl)
        val exception = IllegalStateException("boom")
        adapter.setException(exception)
        adapter.emit()
        assertSame(exception, impl.emittedExceptions.single())
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

    @Test
    fun `test structured body keeps its shape`() {
        val impl = FakeLogger("logger")
        val adapter = OtelJavaLogRecordBuilderAdapter(impl)
        adapter.setBody(Value.of(mapOf("k" to Value.of(listOf(Value.of(1L), Value.of("v"))))))
        adapter.emit()

        val expected = AnyValue.MapValue(
            mapOf("k" to AnyValue.ListValue(listOf(AnyValue.LongValue(1L), AnyValue.StringValue("v"))))
        )
        assertEquals(expected, impl.logs.single().body)
    }

    @Test
    fun `test primitive value body is unwrapped`() {
        val impl = FakeLogger("logger")
        val adapter = OtelJavaLogRecordBuilderAdapter(impl)
        adapter.setBody(Value.of(42L))
        adapter.emit()

        assertEquals(42L, impl.logs.single().body)
    }

    @Test
    fun `test value attribute keeps its shape`() {
        val impl = FakeLogger("logger")
        val adapter = OtelJavaLogRecordBuilderAdapter(impl)
        adapter.setAttribute(AttributeKey.valueKey("map"), Value.of(mapOf("k" to Value.of("v"))))
        adapter.emit()

        val expected = AnyValue.MapValue(mapOf("k" to AnyValue.StringValue("v")))
        assertEquals(expected, impl.logs.single().attributes["map"])
    }
}
