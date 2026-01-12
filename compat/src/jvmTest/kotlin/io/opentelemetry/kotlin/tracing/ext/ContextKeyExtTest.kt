package io.opentelemetry.kotlin.tracing.ext

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaContextKey
import io.opentelemetry.kotlin.context.ContextKeyAdapter
import io.opentelemetry.kotlin.context.toOtelJavaContextKey
import org.junit.Test
import kotlin.test.assertSame

@OptIn(ExperimentalApi::class)
internal class ContextKeyExtTest {

    @Test
    fun toOtelJavaContextKey() {
        val impl = OtelJavaContextKey.named<String>("test")
        val adapter = ContextKeyAdapter(impl)
        assertSame(impl, adapter.toOtelJavaContextKey())
    }
}
