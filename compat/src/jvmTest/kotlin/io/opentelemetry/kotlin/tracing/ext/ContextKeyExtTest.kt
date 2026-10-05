package io.opentelemetry.kotlin.tracing.ext

import io.opentelemetry.kotlin.aliases.OtelJavaContext
import io.opentelemetry.kotlin.aliases.OtelJavaContextKey
import io.opentelemetry.kotlin.context.ContextAdapter
import io.opentelemetry.kotlin.context.ContextKey
import io.opentelemetry.kotlin.context.ContextKeyAdapter
import io.opentelemetry.kotlin.context.toOtelJavaContextKey
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

internal class ContextKeyExtTest {

    @Test
    fun toOtelJavaContextKey() {
        val impl = OtelJavaContextKey.named<String>("test")
        val adapter = ContextKeyAdapter(impl)
        assertSame(impl, adapter.toOtelJavaContextKey())
    }

    @Test
    fun toOtelJavaContextKeyNonAdapter() {
        val key = object : ContextKey<String> {}
        assertSame(key.toOtelJavaContextKey(), key.toOtelJavaContextKey())
    }

    @Test
    fun toOtelJavaContextKeyInterop() {
        val key = object : ContextKey<String> {}
        val ctx = ContextAdapter(OtelJavaContext.root()).set(key, "value") as ContextAdapter
        assertEquals("value", ctx.impl.get(key.toOtelJavaContextKey()))
    }
}
