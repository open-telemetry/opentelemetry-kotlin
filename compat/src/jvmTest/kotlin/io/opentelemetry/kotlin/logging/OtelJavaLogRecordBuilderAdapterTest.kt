package io.opentelemetry.kotlin.logging

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaContext
import io.opentelemetry.kotlin.aliases.OtelJavaContextKey
import org.junit.Test
import java.time.Instant
import kotlin.test.assertEquals

@OptIn(ExperimentalApi::class)
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
}
