package io.opentelemetry.kotlin.context

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaContextKey
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

@OptIn(ExperimentalApi::class)
internal class ContextKeyAdapterTest {

    @Test
    fun `test context key`() {
        val key = "foo"
        val a = ContextKeyAdapter<String>(OtelJavaContextKey.named(key))
        val sharedKey = OtelJavaContextKey.named<String>(key)
        val b = ContextKeyAdapter<String>(sharedKey)
        val c = ContextKeyAdapter<String>(sharedKey)

        assertEquals(key, a.name)
        assertNotEquals(a, b)
        assertNotEquals(b, c)
    }
}
